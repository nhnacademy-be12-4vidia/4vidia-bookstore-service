package com.nhnacademy._vidiabookstoreservice.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private SimpleMailMessage simpleMailMessage;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("임시 비밀번호 전송 성공")
    void sendTempPassword_success() {
        String email = "test@test.com";
        String tempPassword = "1234qwer!";

        emailService.sendTempPassword(email, tempPassword);

        // mailSender.send()가 실제로 호출되었는지 확인하면서, 매개변수를 캡쳐(Capture)함
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());

        // 캡쳐한 메시지 내용이 맞는지 뜯어봄
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(Objects.requireNonNull(sentMessage.getTo())[0]).isEqualTo(email);
        assertThat(sentMessage.getSubject()).contains("임시 비밀번호");
        assertThat(sentMessage.getText()).contains(tempPassword);
    }

    @Test
    @DisplayName("휴먼 인증코드 전송 성공")
    void sendDormantAuthCode_success() {
        String email = "dormant@test.com";
        String authCode = "123456";

        emailService.sendDormantAuthCode(email, authCode);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(Objects.requireNonNull(sentMessage.getTo())[0]).isEqualTo(email);
        assertThat(sentMessage.getSubject()).contains("휴면 계정 인증코드");
        assertThat(sentMessage.getText()).contains(authCode);
    }
}