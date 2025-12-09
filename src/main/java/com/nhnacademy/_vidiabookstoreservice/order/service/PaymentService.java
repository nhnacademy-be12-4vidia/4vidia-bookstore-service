package com.nhnacademy._vidiabookstoreservice.order.service;

import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentCancelResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentResponse;

public interface PaymentService<T> {
    PaymentResponse getPayment(long orderId);

    PaymentResponse savePayment(PaymentCreateRequest paymentCreateRequest);

    PaymentCancelResponse getPaymentKey(long orderId);

    T confirmPayment(String paymentKey, String orderId, long amount);

    T cancelPayment(String paymentKey, String reason, long amount);
}
