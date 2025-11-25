package com.nhnacademy._vidiabookstoreservice.order.service;

import com.nhnacademy.order.domain.Payment;
import com.nhnacademy.order.domain.dto.PaymentCreateRequest;
import com.nhnacademy.order.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public void savePayment(PaymentCreateRequest paymentCreateRequest) {
        Payment payment = new Payment(paymentCreateRequest);

        paymentRepository.save(payment);
    }


}
