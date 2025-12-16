package com.nhnacademy._vidiabookstoreservice.cart.service.scheduler;

import com.nhnacademy._vidiabookstoreservice.cart.repository.redis.DirtyCartRepository;
import com.nhnacademy._vidiabookstoreservice.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartSyncScheduler {

    private final DirtyCartRepository dirtyCartRepository;
    private final CartService cartService;

    @Scheduled(fixedDelay = 3 * 60 * 1000) // 3분 간격으로 redis -> mysql
    public void syncDirtyCarts() {
        Set<Long> dirtyUserIds = dirtyCartRepository.popAllDirtyUsers();
        if (dirtyUserIds.isEmpty()) {
            return;
        }

        for (Long userId : dirtyUserIds) {
            try {
                cartService.flushCartFromRedisToMySql(userId); // redis -> mysql
            } catch (Exception e) {
                log.error("cart scheduler 실패 userId={}", userId, e);
                dirtyCartRepository.markDirty(userId);
            }
        }
    }
}
