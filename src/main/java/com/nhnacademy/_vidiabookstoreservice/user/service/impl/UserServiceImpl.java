package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.user.domain.Address;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.CompleteProfileRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.ChangePasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.DeleteUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UpdateUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.OrderUserResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserInfoResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserProfileResponse;
import com.nhnacademy._vidiabookstoreservice.user.exception.*;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.UserNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.EmailService;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    @Transactional(readOnly = true)
    public UserInfoResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        return UserInfoResponse.fromEntity(user);
    }

    /**
     * 회원 포인트 반환
     */
    @Override
    @Transactional(readOnly = true)
    public Integer getUserByPoint(Long userId){
        User user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);
        return user.getPoint();
    }

    /**
     * 회원 이름 조회
     */
    @Override
    @Transactional(readOnly = true)
    public String getUserName(Long userId) {
        return userRepository.findById(userId)
                .map(User::getName)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderUserResponse getOrderUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Address defaultAddress = user.getAddress();

        return OrderUserResponse.fromEntity(user, defaultAddress);
    }

    /**
     * 회원정보 조회 (마이페이지)
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));
        log.info("user id({}) -> email : {}", userId, user.getEmail());
        return UserProfileResponse.fromEntity(user);
    }

    /**
     * 회원정보 수정
     */
    @Override
    public UserProfileResponse updateUserProfile(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));

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
    @Transactional(readOnly = true)
    public User getProxyById(Long userId) {
        return userRepository.getReferenceById(userId);
    }

    /**
     * 회원 pk 조회
     * */
    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }


    /**
     * 비밀번호 수정
     */
    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));
        // 기존 비밀번호 확인
        if(!BCryptPasswordEncoder.matches(request.currentPassword(),user.getPassword())){
            throw new IncorrectPasswordException();
        }

        // 새 비밀번호와 확인 비밀번호 일치 여부 확인
        if(!request.newPassword().equals(request.confirmPassword())){
            throw new UserNotFoundException();
        }
        // 새 비밀번호가 현재 비밀번호와 동일한지 확인
        if(BCryptPasswordEncoder.matches(request.newPassword(),user.getPassword())){
            throw new UserNotFoundException();
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
                .orElseThrow(() -> new UserNotFoundException(userId));

        if(!BCryptPasswordEncoder.matches(request.currentPassword(), user.getPassword())) { // 순서가 중요?
            throw new IncorrectPasswordException();
        }

        user.setStatus(UserStatus.DELETED); // status → DELETED 로 변경 등
    }

    @Override
    public void updateLastLoginAt(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        user.setLastLoginAt(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public String getUserRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return user.getRole().name();
    }

    @Override
    public void completeProfile(Long userId, CompleteProfileRequest request) { // todo : request에 valid 체크 따로 안하나요?
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));

        // todo : 아래 4개의 if문에 대해서는 왜 예외처리를 안하나요?
        //      null이거나 빈문자열이 들어오면, 해당 값은 설정되지 않고 프로필이 만들어지는건가요?
        if (request.email() != null && !request.email().isBlank()) {
            user.setEmail(request.email());
        }
        if(request.name()!=null && !request.name().isBlank()){
            user.setName(request.name());
        }
        if(request.phone()!=null && !request.phone().isBlank()){
            user.setPhone(request.phone());
        }
        if (request.birthDate() != null) {
            user.setBirthDate(request.birthDate());
        }

        user.setStatus(UserStatus.ACTIVE);
    }


}
