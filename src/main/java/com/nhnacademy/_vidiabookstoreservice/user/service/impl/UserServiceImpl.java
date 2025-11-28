package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.dto.request.*;
import com.nhnacademy._vidiabookstoreservice.user.dto.response.UserProfileResponse;
import com.nhnacademy._vidiabookstoreservice.user.exception.*;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // 회원 가입, 조회, 수정, 삭제

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final BCryptPasswordEncoder BCryptPasswordEncoder;

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
        System.out.println(">>> USER SAVED, ID = "+user.getUserId());

        return user.getUserId();

    }

    /**
     * 아이디 찾기 (이름 + 생일 + 전화번호)
     */
    public String findUserId(FindIdRequest request) {
        LocalDate birthday = LocalDate.parse(request.birthday());
        User user = userRepository.findByNameAndBirthDateAndPhone(
                        request.name(),birthday,request.phone()
                )
                .orElseThrow(()-> new IllegalArgumentException("일치하는 회원정보가 없습니다."));
        return user.getEmail();
    }

    /**
     * 회원정보 조회 (마이페이지)
     */
    @Override
    public UserProfileResponse getUserInfo(Long id) {
        User user = userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("해당하는 유저를 찾을 수 없습니다. "));
        log.info("user id({}) -> email : {}", id, user.getEmail());
        return UserProfileResponse.from(user);
    }

    /**
     * 회원정보 수정
     */
    @Override
    public void updateUserInfo(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("해당하는 유저를 찾을 수 없습니다."));

        if(request.name()!=null && !request.name().isBlank()){
            user.setName(request.name());
        }
        if(request.phone()!=null && !request.phone().isBlank()){
            user.setPhone(request.phone());
        }
        log.info("회원정보 수정완료");
    }

    @Override
    public User getProxyById(Long userId) {
        return userRepository.getReferenceById(userId);
    }


    /**
     * 비밀번호 수정
     */
    @Override
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
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
        user.setUpdatedAt(LocalDateTime.now());
        // 트랜잭션 종료시점에 자동으로 업데이트 됨
    }

    /**
     * 회원 탈퇴
     */
    @Override
    public void deleteUserById(Long id, DeleteUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("회원이 존재하지 않습니다."));

        if(!BCryptPasswordEncoder.matches(request.currentPassword(), user.getPassword())) { // 순서가 중요?
            throw new IncorrectPasswordException("비밀번호가 일치하지 않습니다.");
        }

        user.setStatus(UserStatus.DELETED); // status → DELETED 로 변경 등
    }






}
