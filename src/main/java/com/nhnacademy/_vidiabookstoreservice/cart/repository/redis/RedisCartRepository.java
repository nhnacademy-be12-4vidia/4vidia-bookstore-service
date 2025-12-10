package com.nhnacademy._vidiabookstoreservice.cart.repository.redis;

import com.nhnacademy._vidiabookstoreservice.cart.domain.CartOwner;
import com.nhnacademy._vidiabookstoreservice.cart.domain.enums.CartOwnerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 회원/비회원 장바구니 내용을 Redis에 저장하는 창고
 */
@Slf4j
@RequiredArgsConstructor
@Repository
public class RedisCartRepository {
    private final StringRedisTemplate cartRedisTemplate;
//    public RedisCartRepository(
//            @Qualifier("cartRedisTemplate") StringRedisTemplate redisTemplate
//    ) {
//        this.redisTemplate = redisTemplate;
//    }

    private static final String USER_PREFIX = "cart:user:";
    private static final String GUEST_PREFIX = "cart:guest:";
    private static final Duration TTL = Duration.ofDays(3);

    // 키 생성
    private String key(CartOwner owner) {
        return (owner.type() == CartOwnerType.USER ? USER_PREFIX : GUEST_PREFIX) + owner.id();
    }

    private void refreshTtlIfKeyExists(String key) {
        boolean exists = cartRedisTemplate.hasKey(key);
        if (exists) {
            boolean ok = Boolean.TRUE.equals(cartRedisTemplate.expire(key, TTL));
            if (!ok) {
                // 실제 운영에서는 로거로 기록하세요.
                log.warn("Redis TTL 갱신 실패, key={}", key);
            }
        }
    }

    // 장바구니 전체 조회
    public Map<Long, Integer> getCartItems(CartOwner owner) {
        String key = key(owner);

        Map<Object, Object> entries = cartRedisTemplate.opsForHash().entries(key);
        Map<Long, Integer> result = new HashMap<>();
        entries.forEach((k, v) -> {
            Long bookId = Long.valueOf((String) k);
            Integer quantity = Integer.valueOf((String) v);
            result.put(bookId, quantity);
        });

        return result;
    }

    // 장바구니 도서 수량 수정
    public void setItemQuantity(CartOwner owner, Long bookId, int quantity) {
        String key = key(owner);
        cartRedisTemplate.opsForHash().put(key, String.valueOf(bookId), String.valueOf(quantity));
        refreshTtlIfKeyExists(key);
    }

    public void incrementItemQuantity(CartOwner owner, Long bookId, int delta) {
        String key = key(owner);

        Long newVal = cartRedisTemplate.opsForHash()
                .increment(key, String.valueOf(bookId), delta);

        if (newVal <= 0) {
            cartRedisTemplate.opsForHash().delete(key, String.valueOf(bookId));
        }

        refreshTtlIfKeyExists(key);
    }


    // 장바구니 도서 제거
    public void removeItem(CartOwner owner, Long bookId) {
        String key = key(owner);
        cartRedisTemplate.opsForHash().delete(key, String.valueOf(bookId));

        refreshTtlIfKeyExists(key);
    }

    // 장바구니 비우기 : 키 삭제
    public void clearCart(CartOwner owner) {
        cartRedisTemplate.delete(key(owner));
    }
}
