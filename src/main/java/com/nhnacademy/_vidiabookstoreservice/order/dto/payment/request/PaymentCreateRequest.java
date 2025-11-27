package com.nhnacademy._vidiabookstoreservice.order.dto.payment.request;


import com.nhnacademy._vidiabookstoreservice.order.domain.Order;

public record PaymentCreateRequest(
        Order order,
        String payStatus,
        String payMethod,
        long amount,
        String paymentKey,
        String sendOrderId
) {
}
