package com.nhnacademy._vidiabookstoreservice.order.service;

import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCreateRequest;

public interface PaymentService<T> {
    void savePayment(PaymentCreateRequest paymentCreateRequest);

    T confirmPayment(String paymentKey, String orderId, long amount);

    T cancelPayment(String paymentKey, long amount);
}
