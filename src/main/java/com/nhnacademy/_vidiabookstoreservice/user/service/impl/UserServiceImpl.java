package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindIdRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindPasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.ChangePasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.DeleteUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UpdateUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.OrderUserResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserInfoResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserProfileResponse;
import com.nhnacademy._vidiabookstoreservice.user.exception.*;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.EmailService;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder BCryptPasswordEncoder;
    private final EmailService mailService;

    /**
     * 이메일로 회원 조회
     * */
    @Override
    public UserInfoResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundByEmailException(email));

        return UserInfoResponse.fromEntity(user);
    }

    /**
     * 회원 포인트 반환
     */
    @Override
    public Integer getUserByPoint(Long userId){
        User user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);
        return user.getPoint();
    }

    /**
     * 회원 이름 조회
     */
    @Override
    public String getUserName(Long userId) {
        return userRepository.findById(userId)
                .map(User::getName)
                .orElseThrow(() -> new UserNotFoundByUserIdException(userId));
    }

    @Override
    public OrderUserResponse getOrderUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundByUserIdException(userId));
        return OrderUserResponse.fromEntity(user);
    }

    /**
     * 회원정보 조회 (마이페이지)
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundByUserIdException(userId));
        log.info("user id({}) -> email : {}", userId, user.getEmail());
        return UserProfileResponse.fromEntity(user);
    }

    /**
     * 회원정보 수정
     */
    @Override
    public UserProfileResponse updateUserProfile(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundByUserIdException(userId));

        if(request.name()!=null && !request.name().isBlank()){
            user.setName(request.name());
        }
        if(request.phone()!=null && !request.phone().isBlank()){
            user.setPhone(request.phone());
        }
        log.info("회원정보 수정완료");

        userRepository.save(user);
        return UserProfileResponse.fromEntity(user);
    }

    /**
     * 이건 뭐죠? 지연로딩??
     */
    @Override
    public User getProxyById(Long userId) {
        return userRepository.getReferenceById(userId);
    }

    /**
     * 회원 pk 조회
     * */
    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundByUserIdException(userId));
    }


    /**
     * 비밀번호 수정
     */
    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundByUserIdException(userId));
        // 기존 비밀번호 확인
        if(!BCryptPasswordEncoder.matches(request.currentPassword(),user.getPassword())){
            throw new IncorrectPasswordException();
        }

        // 새 비밀번호와 확인 비밀번호 일치 여부 확인
        if(!request.newPassword().equals(request.confirmPassword())){
            throw new PasswordNotMatchException();
        }
        // 새 비밀번호가 현재 비밀번호와 동일한지 확인
        if(BCryptPasswordEncoder.matches(request.newPassword(),user.getPassword())){
            throw new SameAsOldPasswordException();
        }

        // 비밀번호 암호화 및 업데이트
        String encodeNewPassword = BCryptPasswordEncoder.encode(request.newPassword());
        user.updateEncodedPassword(encodeNewPassword);
    }

    /**
     * 회원 탈퇴
     */
    @Override
    public void deleteUserById(Long userId, DeleteUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundByUserIdException(userId));

        if(!BCryptPasswordEncoder.matches(request.currentPassword(), user.getPassword())) { // 순서가 중요?
            throw new IncorrectPasswordException();
        }

        user.setStatus(UserStatus.DELETED); // status → DELETED 로 변경 등
    }

    /**
     * 아이디 찾기 (이름 + 생일 + 전화번호)
     */
    @Override
    public String findUserId(FindIdRequest request) {
        LocalDate birthday = LocalDate.parse(request.birthday());
        User user = userRepository.findByNameAndBirthDateAndPhone(
                        request.name(),birthday,request.phone()
                )
                .orElseThrow(()-> new UserNotFoundException());
        return user.getEmail();
    }


    /**
     * 비밀번호 찾기 ( 아이디 + 이름 + 전화번호) -> 임시 비밀번호 발급
     */
    @Override
    public String restPasswordAndSendMail (FindPasswordRequest request) {
        User user = userRepository.findByEmailAndNameAndPhone(
                request.email(),request.name(),request.phone()
        ).orElseThrow(()-> new UserNotFoundByEmailException(request.email()));

        // 임시 비밀번호 생성
        String tempPassword = generateTempPassword(10);

        // 비밀번호 암호화 후 저장

        String encodedPassword = BCryptPasswordEncoder.encode(tempPassword);
        user.updateEncodedPassword(encodedPassword);
        userRepository.save(user);

        // 이메일 발송 ( 구체 로직은 MailService에서)
        mailService.sendTempPassword(user.getEmail(), tempPassword);
        return "임시 비밀번호가 이메일로 발송되었습니다.";

    }
    // 임시 비밀번호 발급 로직
    private String generateTempPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    public void updateLastLoginAt(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundByEmailException(email));

        user.setLastLoginAt(LocalDateTime.now());
    }
}
