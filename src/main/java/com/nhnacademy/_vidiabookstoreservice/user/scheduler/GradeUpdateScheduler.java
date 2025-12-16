package com.nhnacademy._vidiabookstoreservice.user.scheduler;

import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;

import com.nhnacademy._vidiabookstoreservice.user.service.GradeService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;



@Slf4j
@Component
@RequiredArgsConstructor
public class GradeUpdateScheduler {
    private final GradeService gradeService;


    // 매월 1일 00:00에 등급 재산정
    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul")
    //테스트용
//    @Scheduled(initialDelay = 10_000, fixedDelay = Long.MAX_VALUE)
    @Transactional
    public void updateUserGrade(){
        long startTime = System.currentTimeMillis();

        int updated = gradeService.recalculateMonthlyGrades();

        long elapsedMs = System.currentTimeMillis() - startTime;
        log.info("[GradeScheduler] 월간 등급 산정 완료 - 변경: {}명, 소요: {}ms", updated, elapsedMs);
    }


}
