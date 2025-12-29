package com.nhnacademy._vidiabookstoreservice.book.mq.producer;

import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.event.DiscountPolicyChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscountPolicyProducer {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void sendChangedEvent(Long categoryId, String eventType) {
        DiscountPolicyChangedEvent event = new DiscountPolicyChangedEvent(
            categoryId, 
            eventType, 
            java.time.LocalDateTime.now()
        );
        
        rabbitTemplate.convertAndSend(
            "discount.exchange", 
            "discount.policy.changed",
            event
        );
        
        log.info("할인 정책 변경 이벤트 발송: categoryId={}, eventType={}", categoryId, eventType);
    }
}
