package com.nhnacademy._vidiabookstoreservice.order.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqInitializer {

    private final RabbitAdmin rabbitAdmin;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info(">>> [RabbitMQ] 강제 초기화 시도...");

        // 연결이 잘 되어 있는지 확인 (실패 시 여기서 에러 터짐)
        rabbitAdmin.initialize();

        log.info(">>> [RabbitMQ] 초기화 완료!");
    }

}
