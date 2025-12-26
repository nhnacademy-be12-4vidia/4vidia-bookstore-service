package com.nhnacademy._vidiabookstoreservice.user.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@RequiredArgsConstructor
@Repository
public class RedisSignupEmailAuthRepository {

    // signup 전용 RedisTemplate 주입
    @Qualifier("signupRedisTemplate")
    private final StringRedisTemplate signupRedisTemplate;

    /**
     * Key prefix
     * 운영 환경(DB 380)에서도 충돌 방지를 위해 prefix 사용
     */
    private static final String PREFIX = "signup:email:auth:";

    /**
     * 인증코드 유효시간
     * (UI: 3분 / 서버: 3분 + 5초 버퍼)
     */
    private static final Duration TTL = Duration.ofSeconds(185); // 180 + 5



    private String key(String email) {
        return PREFIX + email;
    }

    /**
     * 인증코드 저장
     */
    public void saveCode(String email, String code) {
        signupRedisTemplate.opsForValue().set(key(email), code, TTL);
    }

    /**
     * 인증코드 조회
     */
    public String getCode(String email) {
        return signupRedisTemplate.opsForValue().get(key(email));
    }

    /**
     * 인증코드 삭제
     */
    public void deleteCode(String email) {
        signupRedisTemplate.delete(key(email));
    }



}
