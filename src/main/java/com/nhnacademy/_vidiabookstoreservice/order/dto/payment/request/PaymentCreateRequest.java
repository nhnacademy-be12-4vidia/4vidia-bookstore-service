package com.nhnacademy._vidiabookstoreservice.order.dto.payment.request;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.TossPaymentResponse;

public record PaymentCreateRequest(
        Order order,
        String payStatus,
        String payMethod,
        long amount,
        String paymentKey,
        String sendOrderId
) {
    public static PaymentCreateRequest from(Order order, TossPaymentResponse tossPaymentResponse) {
        return new PaymentCreateRequest(
                order,
                tossPaymentResponse.status(),
                tossPaymentResponse.method(),
                tossPaymentResponse.totalAmount(),
                tossPaymentResponse.paymentKey(),
                tossPaymentResponse.orderId()
        );
    }
}
