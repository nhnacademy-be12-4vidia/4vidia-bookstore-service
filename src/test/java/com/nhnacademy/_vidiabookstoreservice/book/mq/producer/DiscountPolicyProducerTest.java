package com.nhnacademy._vidiabookstoreservice.book.mq.producer;

import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.event.DiscountPolicyChangedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DiscountPolicyProducerTest {

    @InjectMocks
    private DiscountPolicyProducer discountPolicyProducer;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("이벤트 발송 성공 - 지정된 Exchange와 RoutingKey로 메시지가 전송된다")
    void sendChangedEvent_Success() {
        Long categoryId = 1L;
        String eventType = "CREATED";

        String expectedExchange = "discount.exchange";
        String expectedRoutingKey = "discount.policy.changed";

        discountPolicyProducer.sendChangedEvent(categoryId, eventType);

        ArgumentCaptor<DiscountPolicyChangedEvent> captor = // LocalDateTime.now()라 직접비교불가
                ArgumentCaptor.forClass(DiscountPolicyChangedEvent.class);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(expectedExchange),
                eq(expectedRoutingKey),
                captor.capture()
        );
        DiscountPolicyChangedEvent capturedEvent = captor.getValue();

        assertThat(capturedEvent.categoryId()).isEqualTo(categoryId);
        assertThat(capturedEvent.eventType()).isEqualTo(eventType);

        assertThat(capturedEvent.changedAt()).isNotNull();
        assertThat(capturedEvent.changedAt()).isBeforeOrEqualTo(LocalDateTime.now()); // 최근인지
    }
}