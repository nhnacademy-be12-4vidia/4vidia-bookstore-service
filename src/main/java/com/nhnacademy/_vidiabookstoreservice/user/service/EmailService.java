package com.nhnacademy._vidiabookstoreservice.user.service;

import com.nhnacademy._vidiabookstoreservice.user.exception.EmailSendFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendTempPassword(String toEmail, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[4VIDIA] 임시 비밀번호 안내");
        message.setText(
                "임시 비밀번호는 다음과 같습니다:\n\n" + tempPassword +
                        "\n\n로그인 후 반드시 비밀번호를 변경해주세요."
        );

        try {
            mailSender.send(message);
        } catch (MailException e) {
            // ✅ 실패를 명확히 실패로 처리
            throw new EmailSendFailedException(e);
        }
    }

    public void sendDormantAuthCode(String toEmail, String authCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[4VIDIA] 휴면 계정 인증코드 안내");
        message.setText(
                "안녕하세요.\n\n" +
                        "휴면 계정 인증코드는 다음과 같습니다:\n\n" +
                        authCode + "\n\n" +
                        "인증 유효시간: 5분\n" +
                        "5분 이내에 인증을 완료해주세요.\n"
        );

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new EmailSendFailedException(e);
        }
    }
}
