package com.nhnacademy._vidiabookstoreservice.cart.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 장바구니 수정 한 회원 userId만 모아두는 저장소
 */
@RequiredArgsConstructor
@Repository
public class DirtyCartRepository {
    private final StringRedisTemplate cartRedisTemplate;

    private static final String DIRTY_USERS_KEY = "cart:dirty:users";

    // 회원 장바구니 변경되면 호출
    public void markDirty(Long userId) {
        cartRedisTemplate.opsForSet().add(DIRTY_USERS_KEY, String.valueOf(userId));
    }

    // 장바구니 변경 된 회원 아이디 리스트 반환
    public Set<Long> popAllDirtyUsers() {
        Set<String> members = cartRedisTemplate.opsForSet().members(DIRTY_USERS_KEY);
        if (members == null || members.isEmpty()) {
            return Set.of();
        }
        cartRedisTemplate.delete(DIRTY_USERS_KEY);

        return members.stream()
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

    // 정상 로그아웃 시 호출 -> dirty set 에서 해당 userId만 제거
    public void remove(Long userId) {
        cartRedisTemplate.opsForSet()
                .remove(DIRTY_USERS_KEY, String.valueOf(userId));
    }

}

