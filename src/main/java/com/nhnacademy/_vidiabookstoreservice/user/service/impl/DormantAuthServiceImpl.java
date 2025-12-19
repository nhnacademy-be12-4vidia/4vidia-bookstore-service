package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.exception.AuthCodeExpiredException;
import com.nhnacademy._vidiabookstoreservice.user.exception.invalid.InvalidAuthCodeException;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.redis.RedisDormantAutoRepository;
import com.nhnacademy._vidiabookstoreservice.user.sender.DoorayMessageSender;
import com.nhnacademy._vidiabookstoreservice.user.service.DormantAuthService;
import com.nhnacademy._vidiabookstoreservice.user.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class DormantAuthServiceImpl implements DormantAuthService {

    private final RedisDormantAutoRepository autoRepository;
    private final DoorayMessageSender webhookSender;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public void sendAuthCode(String email, String webhookUrl) {
        String code = generateCode();

        // 1) Redis에 코드 저장 (TTL은 RedisDormantAutoRepository에서 설정했다고 가정)
        autoRepository.saveCode(email, code);

        // 2) Dooray 메시지
        String title = "[4VIDIA Bookstore] 휴면 계정 인증코드";
        String body  = "인증코드 : " + code + "\n\n"
                + "※ 5분 안에 인증을 완료해주세요.";

        // 사용자가 입력한 Webhook URL로 전송
        webhookSender.send(webhookUrl, title, body);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(900000) + 100000; // 100000~999999
        return String.valueOf(num);
    }

    @Override
    public void verifyAuthCode(String email, String code) {
        String savedCode = autoRepository.getCode(email);

        if (savedCode == null) {
            throw new AuthCodeExpiredException();
        }

        if (!code.equals(savedCode)) {
            throw new InvalidAuthCodeException("INVALID");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(); // 필요하면 커스텀 예외로 변경

        user.setStatus(UserStatus.ACTIVE);

        // 사용 후 코드 삭제
        autoRepository.deleteCode(email);
    }

    @Override
    public void sendAuthCodeByEmail(String loginEmail, String sendToEmail) {
        String code = generateCode();
        String normalizedLoginEmail = loginEmail.trim().toLowerCase();

        // 로그인 이메일을 키로 등록
        autoRepository.saveCode(normalizedLoginEmail, code);

        // 인증 받을 이메일로 발송
        emailService.sendDormantAuthCode(sendToEmail, code);
    }
}