package com.nhnacademy._vidiabookstoreservice.order.mq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendDelayedCancelMessage(Long orderId) {
        rabbitTemplate.convertAndSend(
                "waiting.exchange", // 대기실
                "order.wait", // 라우팅키
                orderId
        );
        log.debug("[Producer] Send delayed cancel message after 15m for orderId={}", orderId);
    }
}
