package com.nhnacademy._vidiabookstoreservice.user.repository.redis;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@RequiredArgsConstructor
@Repository
public class RedisDormantAutoRepository {

    private final StringRedisTemplate humanRedisTemplate;

//    public RedisDormantAutoRepository(
//            @Qualifier("humanRedisTemplate") StringRedisTemplate redisTemplate
//    ) {
//        this.redisTemplate = redisTemplate;
//    }
    private static final String PREFIX = "dormant:";
    private static final Duration TTL = Duration.ofMinutes(5); // 인증코드 유효시간 5분

    private String key(String email){
        return PREFIX + email;
    }


    /**
     * 인증코드 저장
     */
    public void saveCode(String email, String code){
        humanRedisTemplate.opsForValue().set(key(email), code, TTL);
    }

    /**
     * 인증코드 조회
     */
    public String getCode(String email){
        return humanRedisTemplate.opsForValue().get(key(email));
    }

    /**
     * 인증 코드 삭제
     */
    public void deleteCode(String email){
        humanRedisTemplate.delete(key(email));
    }
}
