package com.nhnacademy._vidiabookstoreservice.user.scheduler;


import com.nhnacademy._vidiabookstoreservice.user.service.GradeService;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GradeScheduler {

    private final GradeService gradeService;

    @Scheduled(cron = "0 10 0 1 * *", zone = "Asia/Seoul")
    public void run(){
        gradeService.updateUserGradesMonthly();
    }

}
