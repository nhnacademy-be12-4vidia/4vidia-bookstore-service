package com.nhnacademy.order.domain.dto;

public record PaymentCreateRequest(
        long orderId,
        String payStatus,
        String payMethod,
        long amount,
        String paymentKey,
        String sendOrderId
) {
}
