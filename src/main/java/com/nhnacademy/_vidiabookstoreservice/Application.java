package com.nhnacademy._vidiabookstoreservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class Application {



    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// todo 비회원 좋아요 버튼 어떻게 처리? (영재님 요청)