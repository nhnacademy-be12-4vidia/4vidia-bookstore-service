package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.domain.Payment;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public void savePayment(PaymentCreateRequest paymentCreateRequest) {
        Payment payment = Payment.builder()
                .order(paymentCreateRequest.order())
                .payStatus(paymentCreateRequest.payStatus())
                .payMethod(paymentCreateRequest.payMethod())
                .amount(paymentCreateRequest.amount())
                .paymentKey(paymentCreateRequest.paymentKey())
                .sendOrderId(paymentCreateRequest.sendOrderId())
                .build();

        paymentRepository.save(payment);
    }


}
