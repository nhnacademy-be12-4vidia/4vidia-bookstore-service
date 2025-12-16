package com.nhnacademy._vidiabookstoreservice.order.service.event;

import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.dto.event.BestSellerUpdateEvent;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BestSellerEventListener {
    private final OrderItemRepository orderItemRepository;
    private final StringRedisTemplate bestsellerRedisTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBestSellerUpdate(BestSellerUpdateEvent event) {
        log.info("베스트셀러 업데이트 시작 - OrderId: {}", event.orderId());

        try {
            List<OrderItem> orderItems = orderItemRepository.findByOrder_orderId(event.orderId());

            orderItems.forEach(orderItem ->
                    bestsellerRedisTemplate.opsForZSet().incrementScore("bestseller", orderItem.getBook().getId().toString(), orderItem.getQuantity())
            );
        } catch (Exception e) {
            log.error("베스트셀러 업데이트 실패");
        }
    }
}
