package com.nhnacademy._vidiabookstoreservice.order.mq.producer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderMessageProducerTest {

    @InjectMocks
    private OrderMessageProducer orderMessageProducer;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("지연 취소 메시지 전송 성공 - 지정된 Exchange와 RoutingKey로 주문 ID를 전송한다")
    void sendDelayedCancelMessage_Success() {
        Long orderId = 12345L;
        String expectedExchange = "waiting.exchange";
        String expectedRoutingKey = "order.wait";

        orderMessageProducer.sendDelayedCancelMessage(orderId);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(expectedExchange),
                eq(expectedRoutingKey),
                eq(orderId)
        );
    }
}