package com.nhnacademy._vidiabookstoreservice.user.scheduler;


import com.nhnacademy._vidiabookstoreservice.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
@Slf4j
public class UserDormantScheduler {
    private final AuthService authService;

    /**
     * 매일 새벽 3시 실행
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void markDormantUsers(){
        LocalDateTime threeMonthAgo = LocalDateTime.now().minusMonths(3);
        int count = authService.convertDormantUsers(threeMonthAgo);
        log.info("[DormantScheduler] 휴면 전환 완료 - 대상: {}명", count);
    }






}
