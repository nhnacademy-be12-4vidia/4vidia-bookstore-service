package com.nhnacademy._vidiabookstoreservice.user.service.event;

import com.nhnacademy._vidiabookstoreservice.global.client.CouponClient;
import com.nhnacademy._vidiabookstoreservice.user.dto.event.WelcomeCouponIssueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelcomeCouponEventListener {

    private final CouponClient couponClient;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWelcomeCoupon(WelcomeCouponIssueEvent event) {
        log.info("회원가입 웰컴쿠폰 발행 시작 - UserId: {}", event.userId());

        try {
            couponClient.getRegisterCoupon(event.userId());
        } catch (Exception e) {
            log.error("웰컴쿠폰 발행 실패");
        }
    }

}
