package com.nhnacademy._vidiabookstoreservice.user.scheduler;

import com.nhnacademy._vidiabookstoreservice.global.client.CouponClient;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.dto.coupon.response.ActivePolicyIdResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.event.CouponIssueEvent;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BirthdayCouponScheduler {

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final CouponClient couponClient;

    @Scheduled(cron = "0 0 3 * * *")
    public void issueBirthdayCoupons() {

        // 1️⃣ 활성 생일 쿠폰 정책 조회
        ResponseEntity<ActivePolicyIdResponse> res;
        try {
            res = couponClient.getActivePolicy("BIRTHDAY");
        } catch (Exception e) {
            log.warn("⚠️ 쿠폰 서비스 오류 → 생일 쿠폰 스킵", e);
            return;
        }

        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            log.info("ℹ️ 활성 생일 쿠폰 정책 없음 → 발급 안 함");
            return;
        }

        Long policyId = res.getBody().policyId();

        // 2️⃣ 이번 달 생일 유저 전부 조회
        int month = LocalDate.now().getMonthValue();
        List<User> users =
                userRepository.findBirthdayUsersByMonth(UserStatus.ACTIVE, month);

        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expireAt = issuedAt.plusDays(7);

        // 3️⃣ 전부 발급 요청 (중복은 DB에서 컷)
        for (User user : users) {
            CouponIssueEvent event = new CouponIssueEvent(
                    user.getUserId(),
                    policyId,
                    issuedAt,
                    expireAt
            );

            rabbitTemplate.convertAndSend(
                    "coupon4.exchange",
                    "coupon4.event.requested",
                    event
            );

            log.info("🎂 생일 쿠폰 발급 요청 userId={}", user.getUserId());
        }
    }

}
