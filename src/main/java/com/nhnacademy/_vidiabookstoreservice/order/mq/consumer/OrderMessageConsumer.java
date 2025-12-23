package com.nhnacademy._vidiabookstoreservice.order.mq.consumer;

import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageConsumer {
    private final OrderService orderService;

    @RabbitListener(queues = "processing.queue")
    public void receiveMessage(Long orderId) {
        log.info("[Consumer] Received orderId={}", orderId);

        try {
            orderService.cancelOrderIfPending(orderId);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
