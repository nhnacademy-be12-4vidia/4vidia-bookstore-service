package com.nhnacademy._vidiabookstoreservice.user.service;

import com.nhnacademy._vidiabookstoreservice.user.exception.EmailSendFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async("taskExecutor")
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


    @Async("taskExecutor")
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

    @Async("taskExecutor")
    public void sendSignupAuthCode(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[4VIDIA] 회원가입 이메일 인증번호 안내");
        message.setText(
                "안녕하세요.\n\n" +
                        "4VIDIA 회원가입을 위한 이메일 인증번호입니다.\n\n" +
                        "인증번호: " + code + "\n\n" +
                        "인증 유효시간: 3분\n" +
                        "3분 이내에 인증을 완료해주세요.\n\n" +
                        "본 메일은 회원가입 요청 시에만 발송됩니다."
        );

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new EmailSendFailedException(e);
        }
    }

}
