package com.nhnacademy._vidiabookstoreservice.user.service.event;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EurekaStatusUpdater {

    private final ApplicationInfoManager applicationInfoManager;

    public EurekaStatusUpdater(ApplicationInfoManager applicationInfoManager) {
        this.applicationInfoManager = applicationInfoManager;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() throws InterruptedException {
        System.out.println("초기화 시작...");
        Thread.sleep(30000); // 초기화 작업 예: 30초
        applicationInfoManager.setInstanceStatus(InstanceStatus.UP);
        System.out.println("초기화 완료, Eureka 상태 UP");
    }
}
