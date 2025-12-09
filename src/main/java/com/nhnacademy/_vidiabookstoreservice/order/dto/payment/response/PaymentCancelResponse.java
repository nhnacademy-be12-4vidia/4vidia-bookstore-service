package com.nhnacademy._vidiabookstoreservice.order.dto.payment.response;

import com.nhnacademy._vidiabookstoreservice.order.domain.Payment;

public record PaymentCancelResponse(
        String paymentKey,
        String orderId,
        Integer amount
) {
    public static PaymentCancelResponse from(Payment payment) {
        return new PaymentCancelResponse(
                payment.getPaymentKey(),
                payment.getSendOrderId(),
                payment.getAmount()
        );
    }
}
