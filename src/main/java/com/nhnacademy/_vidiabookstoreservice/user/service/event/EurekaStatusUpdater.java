package com.nhnacademy._vidiabookstoreservice.user.service.event;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EurekaStatusUpdater {

    private final ApplicationInfoManager applicationInfoManager;

    public EurekaStatusUpdater(ApplicationInfoManager applicationInfoManager) {
        this.applicationInfoManager = applicationInfoManager;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() throws InterruptedException {
        log.info("초기화 시작...");
        Thread.sleep(30000); // 초기화 작업 예: 30초
        applicationInfoManager.setInstanceStatus(InstanceStatus.UP);
        log.info("초기화 완료, Eureka 상태 UP");
    }
}
