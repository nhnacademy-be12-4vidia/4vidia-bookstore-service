package com.nhnacademy._vidiabookstoreservice.cart.repository.redis;

import com.nhnacademy._vidiabookstoreservice.cart.domain.CartOwner;
import com.nhnacademy._vidiabookstoreservice.cart.domain.enums.CartOwnerType;
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
@Slf4j
@RequiredArgsConstructor
@Repository
public class RedisCartRepository {

    private final StringRedisTemplate cartRedisTemplate;

    private static final String USER_CART_PREFIX = "cart:user:";
    private static final String GUEST_CART_PREFIX = "cart:guest:";
    private static final String EXPIRE_PREFIX = "cart:expire:user:";

    // 회원 장바구니 수명 (이벤트 발생용)
    private static final Duration USER_CART_TTL = Duration.ofHours(3);
    // 비회원 장바구니 수명 (메모리 관리용 - DB 저장 안 함)
    private static final Duration GUEST_CART_TTL = Duration.ofDays(3);

    private String cartKey(CartOwner owner) {
        return (owner.type() == CartOwnerType.USER
                ? USER_CART_PREFIX
                : GUEST_CART_PREFIX) + owner.id();
    }

    private String expireKey(Long userId) {
        return EXPIRE_PREFIX + userId;
    }

    /**
     * 장바구니 수명 갱신 (핵심 수정 부분)
     */
    private void refreshTTL(CartOwner owner) {
        String dataKey = cartKey(owner);

        if (owner.isUser()) {
            // [회원] 2-Key 방식 사용
            cartRedisTemplate.opsForValue().set(expireKey(owner.id()), "1", USER_CART_TTL);

            cartRedisTemplate.expire(dataKey, USER_CART_TTL.plusHours(3));
        } else {
            cartRedisTemplate.expire(dataKey, GUEST_CART_TTL);
        }
    }

    public boolean existsKey(CartOwner owner) {
        return cartRedisTemplate.hasKey(cartKey(owner));
    }


    public boolean existsExpireKey(Long userId) {
        return cartRedisTemplate.hasKey(EXPIRE_PREFIX + userId);
    }

    public Map<Long, Integer> getCartItems(CartOwner owner) {
        Map<Object, Object> entries = cartRedisTemplate.opsForHash().entries(cartKey(owner));

        Map<Long, Integer> result = new HashMap<>();
        entries.forEach((k, v) ->
                result.put(Long.valueOf(k.toString()), Integer.valueOf(v.toString()))
        );
        return result;
    }

    public void setItemQuantity(CartOwner owner, Long bookId, int quantity) {
        cartRedisTemplate.opsForHash().put(
                cartKey(owner),
                String.valueOf(bookId),
                String.valueOf(quantity)
        );
        refreshTTL(owner);
    }

    public void incrementItemQuantity(CartOwner owner, Long bookId, int delta) {
        Long newVal = cartRedisTemplate.opsForHash().increment(
                cartKey(owner),
                String.valueOf(bookId),
                delta
        );

        if (newVal != null && newVal <= 0) {
            cartRedisTemplate.opsForHash().delete(cartKey(owner), String.valueOf(bookId));
        }
        refreshTTL(owner);
    }

    public void removeItem(CartOwner owner, Long bookId) {
        cartRedisTemplate.opsForHash().delete(cartKey(owner), String.valueOf(bookId));
        refreshTTL(owner);
    }

    public void clearCart(CartOwner owner) {
        cartRedisTemplate.delete(cartKey(owner));

        if (owner.isUser()) {
            cartRedisTemplate.delete(expireKey(owner.id()));
        }
    }
}