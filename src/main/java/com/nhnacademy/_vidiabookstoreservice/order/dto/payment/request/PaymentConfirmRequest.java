package com.nhnacademy.order.domain.dto;

public record PaymentConfirmRequest(
        String paymentKey,
        String orderId,
        int amount
) {
}
