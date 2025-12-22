package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

class AuthControllerTest extends SupportControllerTest {

    @Autowired private UserRepository userRepository;
    @Autowired private GradeRepository gradeRepository;

    private Long testUserId;

    @BeforeEach
    void initData() {

        Grade grade = gradeRepository.save(Grade.builder().gradeName(GradeName.WELCOME).pointRate(1).build());
        User user = User.builder()
                .email("auth_test@test.com")
                .password("password")
                .name("인증테스트")
                .phone("01011112222")
                .birthDate(LocalDate.now())
                .grade(grade)
                .build();
        user.setStatus(UserStatus.ACTIVE);
        this.testUserId = userRepository.save(user).getUserId();
    }

    @Test
    void signup() {
    }

    @Test
    void findOrCreateByPaycoId() {
    }

    @Test
    void findUserId() {
    }

    @Test
    void findPassword() {
    }

    @Test
    void checkEmail() {
    }

    @Test
    void checkDormant() {
    }

    @Test
    void send() {
    }

    @Test
    void verify() {
    }

    @Test
    void sendCodeByEmail() {
    }

    @Test
    void updateLastLoginAt() {
    }
}