package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.sender.DoorayMessageSender;
import com.nhnacademy._vidiabookstoreservice.user.service.DormantAuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DormantAuthServiceImpl implements DormantAuthService {

    private final DoorayMessageSender doorayMessageSender;
    private final UserRepository userRepository;
    // 메모리에 저장 (테스트용)
    private final Map<String, String> codeStore = new ConcurrentHashMap<>();

    // 휴면 계정 : 코드 생성 + 저장 + Dooray 발송
    public void sendDormantCode(String email){
        String code = generateCode();
        codeStore.put(email, code);

        doorayMessageSender.send("휴면 계정 인증","인증코드: "+code);
    }

    public boolean verifyAuthCode(String email, String inputCode) {
        return inputCode.equals(codeStore.get(email));
    }


    @Transactional
    public void activateUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setStatus(UserStatus.ACTIVE);
        user.setLastLoginAt(LocalDateTime.now()); // 인증 성공하면 last_login_at 바꿔줌.
        userRepository.save(user);
        // 인증 성공 후 코드 제거
        codeStore.remove(email);
    }

    private String generateCode()
    {
        return String.valueOf((int)(Math.random() * 9000 + 1000));
    }
}
