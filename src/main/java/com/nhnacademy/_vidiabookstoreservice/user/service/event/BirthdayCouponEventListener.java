package com.nhnacademy._vidiabookstoreservice.user.service.event;

import com.nhnacademy._vidiabookstoreservice.global.client.CouponClient;
import com.nhnacademy._vidiabookstoreservice.user.dto.event.BirthdayCouponIssueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BirthdayCouponEventListener {

    private final CouponClient couponClient;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBirthdayCoupon(BirthdayCouponIssueEvent event) {
        log.info("회원가입 생일쿠폰 발행 시작 - UserId: {}", event.userId());

        try {
            couponClient.getRegisterBirthdayCoupon(event.userId());
        } catch (Exception e) {
            log.error("생일쿠폰 발행 실패");
        }
    }
}
