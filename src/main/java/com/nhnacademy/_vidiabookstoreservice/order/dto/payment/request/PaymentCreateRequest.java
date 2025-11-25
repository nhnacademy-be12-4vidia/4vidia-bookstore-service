package com.nhnacademy._vidiabookstoreservice.order.dto.payment.request;

public record PaymentCreateRequest(
        long orderId,
        String payStatus,
        String payMethod,
        long amount,
        String paymentKey,
        String sendOrderId
) {
}
