package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.dto.request.*;
import com.nhnacademy._vidiabookstoreservice.user.dto.response.UserProfileResponse;
import com.nhnacademy._vidiabookstoreservice.user.exception.*;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.EmailService;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Random;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final BCryptPasswordEncoder BCryptPasswordEncoder;
    private final EmailService mailService;

    /**
     * 회원가입
     */
    @Override
    public Long register(UserSignupRequest request) {
        if(userRepository.existsByEmail(request.email())){
            throw new UserAlreadyExistsException("이미 존재하는 회원입니다. 이메일: %s".formatted(request.email()));
        }

        Grade defaultGrade = gradeRepository.findByGradeName(GradeName.WELCOME);

        // User 생성
        User user = User.builder()
                .email(request.email())
                .password(BCryptPasswordEncoder.encode(request.password()))
                .name(request.name())
                .phone(request.phone())
                .birthDate(request.birthDate())
                .grade(defaultGrade)
                .build();
        log.info("grade : {}", user.getGrade());

        userRepository.save(user);

        return user.getUserId();
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
                .orElseThrow(()-> new IllegalArgumentException("일치하는 회원정보가 없습니다."));
        return user.getEmail();
    }
    /**
     * 비밀번호 찾기 ( 아이디 + 이름 + 전화번호) -> 임시 비밀번호 발급
     */
    public String restPasswordAndSendMail (FindPasswordRequest request) {
        User user = userRepository.findByEmailAndNameAndPhone(
                request.email(),request.name(),request.phone()
        ).orElseThrow(()-> new IllegalArgumentException("일치하는 회원젇보가 없습니다."));

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




    /**
     * 회원정보 조회 (마이페이지)
     */
    @Override
    public UserProfileResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("해당하는 유저를 찾을 수 없습니다. "));
        log.info("user id({}) -> email : {}", userId, user.getEmail());
        return UserProfileResponse.fromEntity(user);
    }

    /**
     * 회원정보 수정
     */
    @Override
    public UserProfileResponse updateUserProfile(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("해당하는 유저를 찾을 수 없습니다."));

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
                .orElseThrow(() -> new UserNotFoundException("회원이 존재하지 않습니다."));
    }


    /**
     * 비밀번호 수정
     */
    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("해당하는 유저를 찾을 수 없습니다."));
        // 기존 비밀번호 확인
        if(!BCryptPasswordEncoder.matches(request.currentPassword(),user.getPassword())){
            throw new IncorrectPasswordException("기존 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호와 확인 비밀번호 일치 여부 확인
        if(!request.newPassword().equals(request.confirmPassword())){
            throw new PasswordNotMatchException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }
        // 새 비밀번호가 현재 비밀번호와 동일한지 확인
        if(BCryptPasswordEncoder.matches(request.newPassword(),user.getPassword())){
            throw new SameAsOldPasswordException("현재 비밀번호와 동일한 비밀번호로는 변경할 수 없습니다.");
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
                .orElseThrow(() -> new UserNotFoundException("회원이 존재하지 않습니다."));

        if(!BCryptPasswordEncoder.matches(request.currentPassword(), user.getPassword())) { // 순서가 중요?
            throw new IncorrectPasswordException("비밀번호가 일치하지 않습니다.");
        }

        user.setStatus(UserStatus.DELETED); // status → DELETED 로 변경 등
    }

}
