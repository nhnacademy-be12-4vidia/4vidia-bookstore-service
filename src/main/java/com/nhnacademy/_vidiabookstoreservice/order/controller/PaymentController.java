package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.OrderStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentConfirmRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.TossPaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import com.nhnacademy._vidiabookstoreservice.order.service.PaymentService;
import com.nhnacademy._vidiabookstoreservice.order.service.impl.TossPaymentServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/orders")
public class PaymentController {

    private final PaymentService<TossPaymentResponse> paymentService;
    private final OrderService orderService;

    PaymentController(TossPaymentServiceImpl tossPaymentService, OrderService orderService) {
        this.paymentService = tossPaymentService;
        this.orderService = orderService;
    }

    @PostMapping("/{orderId}/success")
    public ResponseEntity<Void> savePaymentDetail(@PathVariable long orderId,
                                                  @RequestBody PaymentConfirmRequest confirmRequest) {

        TossPaymentResponse tossPaymentResponse = paymentService.confirmPayment(confirmRequest.paymentKey(), confirmRequest.orderId(), confirmRequest.amount());

        Order order = orderService.getOrder(orderId);

        PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.from(order, tossPaymentResponse);

        paymentService.savePayment(paymentCreateRequest);

        orderService.updateOrderStatus(orderId, OrderStatus.PAID);

        return ResponseEntity.status(HttpStatus.CREATED).build();

        //TODO orders 테이블 주문상태 수정해야하는데 가상계좌가 들어오니 status 뜯어보고 그거에 따라 바꿔줘야하나?
        // 가상계좌면 계좌정보 담아서 보내야함?. 근데 일단 pass
    }

}
