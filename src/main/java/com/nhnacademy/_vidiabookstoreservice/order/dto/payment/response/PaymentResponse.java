package com.nhnacademy._vidiabookstoreservice.order.dto.payment.response;

import com.nhnacademy._vidiabookstoreservice.order.domain.Payment;

public record PaymentResponse(
    Long orderId,
    String payStatus,
    Integer amount
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getOrder().getOrderId(),
                payment.getPayStatus(),
                payment.getAmount()
        );
    }
}
