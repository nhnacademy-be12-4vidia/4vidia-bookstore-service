package com.nhnacademy._vidiabookstoreservice.user.scheduler;


import com.nhnacademy._vidiabookstoreservice.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Component
@RequiredArgsConstructor
@Slf4j
public class UserDormantScheduler {
    private final AuthService authService;

    /**
     * 매일 새벽 00:10에  실행
     */
    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    public void markDormantUsers() {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDateTime threeMonthAgo = LocalDateTime.now(zone).minusMonths(3);

        int count = authService.convertDormantUsers(threeMonthAgo);
        log.info("[DormantScheduler] 휴면 전환 완료 - 대상: {}명", count);
    }


}
