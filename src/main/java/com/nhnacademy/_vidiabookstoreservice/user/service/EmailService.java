package com.nhnacademy._vidiabookstoreservice.user.service;


import lombok.RequiredArgsConstructor;
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
        message.setText("임시 비밀번호는 다음과 같습니다:\n\n"+tempPassword
        +"\n\n 로그인 후 반드시 비밀번호를 변경해주세요.");
        mailSender.send(message);
    }



}
