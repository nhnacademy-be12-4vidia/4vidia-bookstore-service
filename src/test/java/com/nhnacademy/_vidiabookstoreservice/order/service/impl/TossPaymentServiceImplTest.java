package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.Payment;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentCancelResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.TossPaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.exception.PaymentConfirmException;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.PaymentNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TossPaymentServiceImplTest {

    @Spy // 실제 객체 생성 - sendRequest에 필요
    @InjectMocks
    TossPaymentServiceImpl tossPaymentService;

    @Mock
    PaymentRepository paymentRepository;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("결제 내역 조회 성공 - 주문아이디 있음")
    void getPayment_success() {
        long orderId = 1L;
        Payment payment = Payment.builder()
                .order(mock(Order.class))
                .payStatus("DONE")
                .amount(10000)
                .build();

        given(paymentRepository.findPaymentByOrder_orderId(orderId)).willReturn(Optional.of(payment));

        given(payment.getOrder().getOrderId()).willReturn(orderId);
        PaymentResponse response = tossPaymentService.getPayment(orderId);

        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.payStatus()).isEqualTo("DONE");
        assertThat(response.amount()).isEqualTo(10000);
    }

    @Test
    @DisplayName("결제 내역 조회 실패 - 주문아이디 없음")
    void getPayment_fail() {
        given(paymentRepository.findPaymentByOrder_orderId(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy( () -> tossPaymentService.getPayment(anyLong()))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    @DisplayName("결제 내역 추가 성공")
    void savePayment_success() {
        PaymentCreateRequest request = new PaymentCreateRequest(
                Order.builder().build(), "DONE", "CARD", 10000, "Key", "UUID"
        );

        tossPaymentService.savePayment(request);

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("페이먼트키 조회 성공")
    void getPaymentKey_Success() {
        long orderId = 1L;
        String paymentKey = "Key";
        Payment payment = Payment.builder()
                .paymentKey(paymentKey)
                .sendOrderId("UUID-001")
                .amount(10000)
                .build();

        given(paymentRepository.findPaymentByOrder_orderId(orderId)).willReturn(Optional.of(payment));

        PaymentCancelResponse response = tossPaymentService.getPaymentKey(orderId);

        verify(paymentRepository).findPaymentByOrder_orderId(anyLong());
        assertThat(response).isNotNull();
        assertThat(response.paymentKey()).isEqualTo(paymentKey);
    }

    @Test
    @DisplayName("페이먼트키 조회 실패 - 주문아이디 없음")
    void getPaymentKey_Fail_NotFound() {
        given(paymentRepository.findPaymentByOrder_orderId(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> tossPaymentService.getPayment(anyLong()))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    @DisplayName("결제 엔티티 조회 성공")
    void getPaymentEntity_success() {
        Long orderId = 1L;
        Payment payment = Payment.builder().order(mock(Order.class)).build();
        given(paymentRepository.findPaymentByOrder_orderId(orderId)).willReturn(Optional.of(payment));

        Payment result = tossPaymentService.getPaymentEntity(orderId);

        assertThat(result).isEqualTo(payment);
    }

    @Test
    @DisplayName("결제 엔티티 조회 실패 (getPaymentEntity)")
    void getPaymentEntity_fail() {
        given(paymentRepository.findPaymentByOrder_orderId(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> tossPaymentService.getPaymentEntity(anyLong()))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    @DisplayName("결제 승인 성공")
    void confirmPayment_Success() throws Exception {
        String paymentKey = "UUID-";
        String orderId = "ORD-";
        long amount = 10000;

        // ReflectionTestUtils: 테스트용. 임의로 setter 생성, private 로직 검증
        ReflectionTestUtils.setField(tossPaymentService, "API_SECRET_KEY", "secret");

        Map<String, Object> mockData = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "totalAmount", amount
        );

        //spy 객체 호출시 실제 통신 대신 리턴
        doReturn(mockData).when(tossPaymentService).sendRequest(any(), any(), any());

        TossPaymentResponse result = tossPaymentService.confirmPayment(paymentKey, orderId, amount);

        assertThat(result.paymentKey()).isEqualTo(paymentKey);
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.totalAmount()).isEqualTo(amount);
    }

    @Test
    @DisplayName("결제 승인 - 실패: 토스 에러 응답")
    void confirmPayment_Fail_TossError() throws Exception {
        ReflectionTestUtils.setField(tossPaymentService, "API_SECRET_KEY", "secret");

        Map<String, Object> mockData = Map.of(
                "code", "PAYMENT_NOT_FOUND",
                "message", "존재하지 않는 결제입니다."
        );

        //spy 객체 호출시 실제 통신 대신 리턴
        doReturn(mockData).when(tossPaymentService).sendRequest(any(), any(), any());

        assertThatThrownBy(() -> tossPaymentService.confirmPayment(any(), any(), any()))
                .isInstanceOf(PaymentConfirmException.class);
    }

    @Test
    @DisplayName("결제 취소 성공")
    void cancelPayment_Success() throws Exception {
        String paymentKey = "UUID-";
        String cancelReason = "배송 전 취소";
        long cancelAmount = 20000;

        ReflectionTestUtils.setField(tossPaymentService, "API_SECRET_KEY", "secret");

        Map<String, Object> cancelInfo = Map.of(
                "cancelAmount", cancelAmount,
                "cancelReason", cancelReason
        );

        Map<String, Object> mockData = Map.of(
                "paymentKey", paymentKey,
                "status", "CANCELED",
                "cancels", List.of(cancelInfo)
        );

        doReturn(mockData).when(tossPaymentService).sendRequest(any(), any(), any());

        TossPaymentResponse result = tossPaymentService.cancelPayment(paymentKey, cancelReason, cancelAmount);

        assertThat(result.paymentKey()).isEqualTo(paymentKey);
        assertThat(result.status()).isEqualTo("CANCELED");
        assertThat(result.cancels()).hasSize(1);
        assertThat(result.cancels().getFirst().cancelAmount()).isEqualTo(cancelAmount);
        assertThat(result.cancels().getFirst().cancelReason()).isEqualTo(cancelReason);
    }

    @Test
    @DisplayName("결제 취소 - 실패: 토스 에러 응답")
    void cancelPayment_Fail_TossError() throws Exception {
        ReflectionTestUtils.setField(tossPaymentService, "API_SECRET_KEY", "secret");

        Map<String, Object> mockData = Map.of(
                "code", "NOT_CANCELABLE_AMOUNT",
                "message", "취소할 수 없는 금액입니다."
        );

        doReturn(mockData).when(tossPaymentService).sendRequest(any(), any(), any());

        assertThatThrownBy(() -> tossPaymentService.cancelPayment("key", "reason", 20000))
                .isInstanceOf(PaymentConfirmException.class)
                .hasMessageContaining("NOT_CANCELABLE_AMOUNT");
    }

}