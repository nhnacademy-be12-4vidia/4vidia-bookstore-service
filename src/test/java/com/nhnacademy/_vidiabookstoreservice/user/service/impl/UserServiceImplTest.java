package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.netflix.discovery.converters.Auto;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.ChangePasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.exception.IncorrectPasswordException;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceImplTest {

    @Autowired
    UserServiceImpl  userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @MockBean
    EmailService emailService; // 실제 메일 발송 막기

    User user;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup(){
        userRepository.deleteAll();

        user = User.builder()
                .email("test@test.com")
                .name("홍길동")
                .birthDate(LocalDate.of(2000,1,1))
                .phone("01012341234")
                .password(passwordEncoder.encode("1234"))
                .build();
        userRepository.save(user);
    }

    // ---------------------------------
    @Test
    void changerPassword_success(){
        ChangePasswordRequest req = new ChangePasswordRequest("1234","abcd","abcd");

        userService.changePassword(user.getUserId(),req);

        User updated = userRepository.findById(user.getUserId()).get();
        assertThat(passwordEncoder.matches("abcd", updated.getPassword())).isTrue();
    }
    
    @Test
    void changerPassword_fail(){
        ChangePasswordRequest req = new ChangePasswordRequest("wrong","abcd","abcd");

        assertThatThrownBy(()->userService.changePassword(user.getUserId(),req))
                .isInstanceOf(IncorrectPasswordException.class);
    }

}