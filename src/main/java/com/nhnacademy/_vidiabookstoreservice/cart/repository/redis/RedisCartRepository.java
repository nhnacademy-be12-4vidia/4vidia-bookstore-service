package com.nhnacademy._vidiabookstoreservice.cart.repository.redis;

import com.nhnacademy._vidiabookstoreservice.cart.domain.CartOwner;
import com.nhnacademy._vidiabookstoreservice.cart.domain.enums.CartOwnerType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 회원/비회원 장바구니 내용을 Redis에 저장하는 창고
 */
@Repository
public class RedisCartRepository {
    private final StringRedisTemplate redisTemplate;
    public RedisCartRepository(
            @Qualifier("cartRedisTemplate") StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    private static final String USER_PREFIX = "cart:user:";
    private static final String GUEST_PREFIX = "cart:guest:";
    private static final Duration TTL = Duration.ofDays(3); // 비회원 장바구니 기간 3일 (회원은 스케줄러로 처리)

    // 키 생성
    private String key(CartOwner owner) {
        return (owner.type() == CartOwnerType.USER ? USER_PREFIX : GUEST_PREFIX) + owner.id();
    }

    // 비회원 ttl 설정
    private void initGuestTtl(CartOwner owner, String key){
        if(owner.type() == CartOwnerType.USER) return;

        long ttl = redisTemplate.getExpire(key);
        if(ttl == -1 || ttl == -2){ // -1 : TTL 없음, -2 : 키 없음
            redisTemplate.expire(key, TTL);
        }
    }

    // 장바구니 전체 조회
    public Map<Long, Integer> getCartItems(CartOwner owner) {
        String key = key(owner);

        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
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
        redisTemplate.opsForHash().put(key, String.valueOf(bookId), String.valueOf(quantity));
        initGuestTtl(owner, key);
    }

    public void incrementItemQuantity(CartOwner owner, Long bookId, int delta) {
        String key = key(owner);

        Long newVal = redisTemplate.opsForHash()
                .increment(key, String.valueOf(bookId), delta);

        if (newVal <= 0) {
            redisTemplate.opsForHash().delete(key, String.valueOf(bookId));
        }

        initGuestTtl(owner, key);
    }

    // 장바구니 도서 제거
    public void removeItem(CartOwner owner, Long bookId) {
        redisTemplate.opsForHash().delete(key(owner), String.valueOf(bookId));
    }

    // 장바구니 비우기
    public void clearCart(CartOwner owner) {
        redisTemplate.delete(key(owner));
    }


}
