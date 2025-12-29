package com.nhnacademy._vidiabookstoreservice.cart.repository.redis;

import com.nhnacademy._vidiabookstoreservice.cart.domain.CartOwner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 회원/비회원 장바구니 Redis 저장소
 * - Redis는 작업 공간
 * - TTL 만료 이벤트는 외부 Listener에서 처리
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisCartRepository {

    private final StringRedisTemplate cartRedisTemplate;

    private static final String USER_CART_PREFIX = "cart:user:";
    private static final String GUEST_CART_PREFIX = "cart:guest:";
    private static final String EXPIRE_PREFIX = "cart:expire:user:";

    private static final Duration USER_CART_TTL = Duration.ofHours(2); // 장바구니 미접근 2시간 -> 장바구니 만료 이벤트
    private static final Duration GUEST_CART_TTL = Duration.ofDays(2);

    private String cartKey(CartOwner owner) {
        return (owner.isUser() ? USER_CART_PREFIX : GUEST_CART_PREFIX) + owner.id();
    }

    // 만료 이벤트 키
    private String expireKey(Long userId) {
        return EXPIRE_PREFIX + userId;
    }

    // 실제 데이터 키
    public boolean existsDataKey(CartOwner owner) {
        return cartRedisTemplate.hasKey(cartKey(owner));
    }

    /**
     * DataKey가 있을 때만 TTL 재설정
     */
    public void refreshTtlIfDataKeyExists(CartOwner owner) {
        String dataKey = cartKey(owner);
        if (!cartRedisTemplate.hasKey(cartKey(owner))) {
            return;
        }

        if (owner.isUser()) {
            cartRedisTemplate.opsForValue().set(expireKey(owner.id()), "1", USER_CART_TTL);
            cartRedisTemplate.expire(dataKey, USER_CART_TTL.plusHours(3)); // 만료 이벤트 발생 + 3h 후 실제 장바구니 데이터 삭제
        } else {
            cartRedisTemplate.expire(dataKey, GUEST_CART_TTL);
        }
    }

    public boolean existsExpireKey(Long userId) {
        String dataKey = USER_CART_PREFIX + userId;
        String expKey = EXPIRE_PREFIX + userId;

        if (!cartRedisTemplate.hasKey(dataKey)) {
            cartRedisTemplate.delete(expKey);
            return false;
        }
        return cartRedisTemplate.hasKey(expKey);
    }

    public Map<Long, Integer> getCartItems(CartOwner owner) {
        Map<Object, Object> entries = cartRedisTemplate.opsForHash().entries(cartKey(owner));
        Map<Long, Integer> result = new HashMap<>();
        entries.forEach((k, v) -> result.put(Long.valueOf(k.toString()), Integer.valueOf(v.toString())));
        return result;
    }

    public void putAllNoTtl(CartOwner owner, Map<Long, Integer> items) {
        if (items == null || items.isEmpty()) return;

        Map<String, String> toPut = new HashMap<>();
        items.forEach((k, v)
                -> toPut.put(String.valueOf(k), String.valueOf(v)));

        cartRedisTemplate.opsForHash().putAll(cartKey(owner), toPut);
    }

    public void setItemQuantity(CartOwner owner, Long bookId, int quantity) {
        cartRedisTemplate.opsForHash().put(cartKey(owner), String.valueOf(bookId), String.valueOf(quantity));
        refreshTtlIfDataKeyExists(owner);
    }

    public void incrementItemQuantity(CartOwner owner, Long bookId, int delta) {
        Long newVal = cartRedisTemplate.opsForHash()
                .increment(cartKey(owner), String.valueOf(bookId), delta);

        if (newVal != null && newVal <= 0) {
            cartRedisTemplate.opsForHash().delete(cartKey(owner), String.valueOf(bookId));
        }
        refreshTtlIfDataKeyExists(owner);
    }

    public void removeItem(CartOwner owner, Long bookId) {
        cartRedisTemplate.opsForHash().delete(cartKey(owner), String.valueOf(bookId));

        if (isEmpty(owner)) {
            clearCart(owner); 
            return;
        }

        refreshTtlIfDataKeyExists(owner);
    }

    /**
     * TTL 갱신 없이 삭제(조회 중 정리용)
     */
    public void removeNoTtl(CartOwner owner, Long bookId) {
        cartRedisTemplate.opsForHash().delete(cartKey(owner), String.valueOf(bookId));
    }

    public void clearCart(CartOwner owner) {
        cartRedisTemplate.delete(cartKey(owner));
        if (owner.isUser()) {
            cartRedisTemplate.delete(expireKey(owner.id()));
        }
    }

    public boolean isEmpty(CartOwner owner) {
        Long size = cartRedisTemplate.opsForHash().size(cartKey(owner));
        return size == null || size == 0;
    }

}