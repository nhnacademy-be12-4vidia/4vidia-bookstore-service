package com.nhnacademy._vidiabookstoreservice.order.dto.payment.request;

public record PaymentCancelRequest(
        String paymentKey,
        String reason,
        long amount
) {
}
