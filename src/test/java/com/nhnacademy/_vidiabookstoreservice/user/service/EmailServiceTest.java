package com.nhnacademy._vidiabookstoreservice.user.service;

import com.nhnacademy._vidiabookstoreservice.user.exception.EmailSendFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("sendDormantAuthCode: mailSender 실패 시 EmailSendFailedException 발생")
    void sendDormantAuthCode_fail_throwsEmailSendFailedException() {
        // given
        doThrow(new MailSendException("smtp down"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // when & then
        assertThatThrownBy(() -> emailService.sendDormantAuthCode("to@email.com", "123456"))
                .isInstanceOf(EmailSendFailedException.class);
    }

    @Test
    @DisplayName("sendTempPassword: mailSender 실패 시 EmailSendFailedException 발생")
    void sendTempPassword_fail_throwsEmailSendFailedException() {
        // given
        doThrow(new MailSendException("smtp down"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // when & then
        assertThatThrownBy(() -> emailService.sendTempPassword("to@email.com", "tempPw"))
                .isInstanceOf(EmailSendFailedException.class);
    }

    @Test
    @DisplayName("sendDormantAuthCode: 성공 시 mailSender.send 호출")
    void sendDormantAuthCode_success_callsMailSender() {
        // given
        // (성공 케이스는 아무 스텁 안 해도 됨)

        // when
        emailService.sendDormantAuthCode("to@email.com", "123456");

        // then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
