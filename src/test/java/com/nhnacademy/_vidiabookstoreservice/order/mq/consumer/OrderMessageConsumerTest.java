package com.nhnacademy._vidiabookstoreservice.order.mq.consumer;

import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderMessageConsumerTest {

    @InjectMocks
    OrderMessageConsumer orderMessageConsumer;

    @Mock
    OrderService orderService;

    @Test
    @DisplayName("메시지 수신 성공: 서비스를 정상적으로 호출한다")
    void receiveMessage_Success() {
        Long orderId = 1L;

        orderMessageConsumer.receiveMessage(orderId);

        verify(orderService, times(1)).cancelOrderIfPending(orderId);
    }

    @Test
    @DisplayName("메시지 수신 실패: 서비스에서 에러 발생 시 RuntimeException으로 감싸서 던진다")
    void receiveMessage_Fail() {
        Long orderId = 1L;

        doThrow(new IllegalArgumentException("Service Error"))
                .when(orderService).cancelOrderIfPending(orderId);

        assertThatThrownBy(() -> orderMessageConsumer.receiveMessage(orderId))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }
}