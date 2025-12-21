package com.nhnacademy._vidiabookstoreservice.user.service;


import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // todo: 예외 처리 아무것도 안해도 되나요??

    public void sendTempPassword(String toEmail, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[4VIDIA] 임시 비밀번호 안내");
        message.setText("임시 비밀번호는 다음과 같습니다:\n\n"+tempPassword
        +"\n\n 로그인 후 반드시 비밀번호를 변경해주세요.");
        mailSender.send(message);
    }


    //  추가: 휴면 인증코드 전송 메소드
    public void sendDormantAuthCode(String toEmail, String authCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[4VIDIA] 휴면 계정 인증코드 안내");
        message.setText("안녕하세요.\n\n"
                + "휴면 계정 인증코드는 다음과 같습니다:\n\n"
                + authCode + "\n\n"
                + "인증 유효시간: 5분\n"
                + "5분 이내에 인증을 완료해주세요.\n");
        mailSender.send(message);
    }
}


