package com.nhnacademy._vidiabookstoreservice.order.dto.payment.request;

public record PaymentConfirmRequest(
        String paymentKey,
        String orderId,
        int amount
) {
}
