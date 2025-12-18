package com.nhnacademy._vidiabookstoreservice.order.service.event;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.dto.event.BestSellerUpdateEvent;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BestSellerEventListenerTest {

    @InjectMocks
    BestSellerEventListener bestSellerEventListener;

    @Mock
    OrderItemRepository orderItemRepository;

    @Mock
    private StringRedisTemplate bestsellerRedisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Test
    @DisplayName("베스트셀러 업데이트 성공: 주문 아이템 수만큼 Redis 점수 증가")
    void handleBestSellerUpdate_success() {
        Long orderId = 100L;
        BestSellerUpdateEvent bestSellerUpdateEvent = new BestSellerUpdateEvent(orderId);

        Book book1 = mock(Book.class);
        given(book1.getId()).willReturn(1L);

        OrderItem orderItem1 = mock(OrderItem.class);
        given(orderItem1.getBook()).willReturn(book1);
        given(orderItem1.getQuantity()).willReturn(2);

        Book book2 = mock(Book.class);
        given(book2.getId()).willReturn(2L);

        OrderItem orderItem2 = mock(OrderItem.class);
        given(orderItem2.getBook()).willReturn(book2);
        given(orderItem2.getQuantity()).willReturn(1);

        given(orderItemRepository.findByOrder_orderId(orderId)).willReturn(List.of(orderItem1, orderItem2));

        // 어렵다
        given(bestsellerRedisTemplate.opsForZSet()).willReturn(zSetOperations);

        bestSellerEventListener.handleBestSellerUpdate(bestSellerUpdateEvent);

        verify(orderItemRepository).findByOrder_orderId(orderId);

        verify(zSetOperations).incrementScore(
                eq("stats:bestseller:daily"), eq("1"), eq(2.0)
        );

        verify(zSetOperations).incrementScore(
                eq("stats:bestseller:daily"), eq("2"), eq(1.0)
        );
    }

    @Test
    @DisplayName("베스트셀러 업데이트 - 실패: 예외 발생 시 로그 남기고 종료")
    void handleBestSellerUpdate_fail() {
        Long orderId = 100L;
        BestSellerUpdateEvent bestSellerUpdateEvent = new BestSellerUpdateEvent(orderId);

        given(orderItemRepository.findByOrder_orderId(orderId)).willThrow(new RuntimeException("Database Error"));

        bestSellerEventListener.handleBestSellerUpdate(bestSellerUpdateEvent);

        verify(bestsellerRedisTemplate, never()).opsForZSet();
    }
}