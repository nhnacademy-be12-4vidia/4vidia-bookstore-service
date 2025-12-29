package com.nhnacademy._vidiabookstoreservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
@EnableJpaAuditing
@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableCaching
public class Application {

    private final ApplicationInfoManager applicationInfoManager;

    public Application(ApplicationInfoManager applicationInfoManager) {
        this.applicationInfoManager = applicationInfoManager;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner init() {
        return args -> {
            // 1️⃣ 초기화 작업
            loadData();  // DB, 외부 API 호출 등

            // 2️⃣ 초기화 완료 후 상태를 UP으로 변경 -> Eureka에 등록됨
            applicationInfoManager.setInstanceStatus(InstanceStatus.UP);
        };
    }

    private void loadData() throws InterruptedException {
        //  30초
        Thread.sleep(600000);
    }
}