package com.nhnacademy._vidiabookstoreservice.cart.service.scheduler;

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

    private final CartExpireListener cartExpireListener;
    private final CartService cartService;

    // 30분마다 -> 만료된 장바구니 (expire 키) flush
    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void flushExpiredCarts() {
        Set<Long> expiredUserIds = cartExpireListener.consumeExpiredUserIds();

        if (expiredUserIds.isEmpty()) {
            return;
        }

        log.info("만료된 장바구니 flush 시작 대상={}", expiredUserIds);

        expiredUserIds.forEach(userId -> {
            try {
                cartService.flushCartFromRedisToMySql(userId);
            } catch (Exception e) {
                log.error("장바구니 flush 실패 userId={}", userId, e);
            }
        });
    }
}