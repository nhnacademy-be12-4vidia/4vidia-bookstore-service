package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentConfirmRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.TossPaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.PaymentService;
import com.nhnacademy._vidiabookstoreservice.order.service.TossPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class PaymentController {

    private final TossPaymentService tossPaymentService;

    private final PaymentService paymentService;

    @PostMapping("/{orderId}/success")
    public ResponseEntity<Void> savePaymentDetail(@PathVariable long orderId,
                    @RequestBody PaymentConfirmRequest confirmRequest) throws IOException {
        TossPaymentResponse tossPaymentResponse = tossPaymentService.confirmPayment(confirmRequest.paymentKey(), confirmRequest.orderId(), confirmRequest.amount());


        PaymentCreateRequest paymentCreateRequest = new PaymentCreateRequest(
                orderId,
                tossPaymentResponse.status(),
                tossPaymentResponse.method(),
                tossPaymentResponse.totalAmount(),
                tossPaymentResponse.paymentKey(),
                tossPaymentResponse.orderId()
        );

        paymentService.savePayment(paymentCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();

        //TODO orders 테이블 주문상태 수정해야하는데 가상계좌가 들어오니 status 뜯어보고 그거에 따라 바꿔줘야하나?
        // 가상계좌면 계좌정보 담아서 보내야함?. 근데 일단 pass
    }

}
