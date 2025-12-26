package com.nhnacademy._vidiabookstoreservice.user.repository.redis;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RedisSignupEmailVerifiedRepository {
    @Qualifier("signupRedisTemplate")
    private final StringRedisTemplate signupRedisTemplate;

    private static final String PREFIX = "signup:email:verified:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private String key(String email){ return PREFIX + email; }

    public void markVerified(String email){
        signupRedisTemplate.opsForValue().set(key(email), "Y", TTL);
    }

    public boolean isVerified(String email){
        return "Y".equals(signupRedisTemplate.opsForValue().get(key(email)));
    }
    public void clear(String email){
        signupRedisTemplate.delete(key(email));
    }
}
