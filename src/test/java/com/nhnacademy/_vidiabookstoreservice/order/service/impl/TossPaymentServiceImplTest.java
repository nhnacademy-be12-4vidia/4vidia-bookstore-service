package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.Payment;
import com.nhnacademy._vidiabookstoreservice.order.exception.PaymentNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class TossPaymentServiceImplTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    TossPaymentServiceImpl tossPaymentService;

    private final long TEST_ORDER_ID = 1L;
    private final String TEST_PAYMENT_KEY = "test_key";
    private Payment mockPayment;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        mockOrder = Order.builder()
                .build();
        mockOrder.setOrderId(TEST_ORDER_ID);

        mockPayment = Payment.builder()
                .order(mockOrder)
                .payStatus("DONE")
                .payMethod("카드")
                .amount(10000L)
                .paymentKey(TEST_PAYMENT_KEY)
                .sendOrderId("Order_UUID_XYZ")
                .build();

        // 뭐하는친구? - 공부
        // ReflectionTestUtils.setField(tossPaymentService, "API_SECRET_KEY", "test_secret");
    }

    @Test
    @DisplayName("결제 내역 조회 성공 - 주문아이디 있음")
    void getPayment_success() {
    }

    @Test
    @DisplayName("결제 내역 조회 실패 - 주문아이디 없음")
    void getPayment_fail() {
    }

    @Test
    @DisplayName("결제 내역 추가")
    void savePayment() {
    }

    @Test
    void getPaymentKey() {
    }

    @Test
    @DisplayName("결제 엔티티 조회 성공")
    void getPaymentEntity_success() {
        given(paymentRepository.findPaymentByOrder_orderId(TEST_ORDER_ID)).willReturn(Optional.of(mockPayment));

        Payment result = tossPaymentService.getPaymentEntity(TEST_ORDER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getPaymentKey()).isEqualTo(TEST_PAYMENT_KEY);
        verify(paymentRepository, times(1)).findPaymentByOrder_orderId(TEST_ORDER_ID);
    }

    @Test
    @DisplayName("결제 엔티티 조회 실패 (getPaymentEntity)")
    void getPaymentEntity_fail() {
        // GIVEN
        given(paymentRepository.findPaymentByOrder_orderId(anyLong())).willReturn(Optional.empty());

        // WHEN & THEN
        //assertThatThrownBy(() -> tossPaymentService.getPaymentEntity(99L))
               // .isInstanceOf(PaymentNotFoundException.class);
    }
}