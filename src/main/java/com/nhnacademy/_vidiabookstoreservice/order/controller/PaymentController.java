package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCancelRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.TossPaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import com.nhnacademy._vidiabookstoreservice.order.service.PaymentService;
import com.nhnacademy._vidiabookstoreservice.order.service.impl.TossPaymentServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class PaymentController {

    private final PaymentService<TossPaymentResponse> paymentService;
    private final OrderService orderService;

    PaymentController(TossPaymentServiceImpl tossPaymentService, OrderService orderService) {
        this.paymentService = tossPaymentService;
        this.orderService = orderService;
    }

    @GetMapping("/pay/{orderId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable long orderId) {
        PaymentResponse paymentResponse = paymentService.getPayment(orderId);

        return ResponseEntity.status(HttpStatus.OK).body(paymentResponse);
    }

    @PostMapping("/{orderId}/cancel") //배송 전 전액취소 상황
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable long orderId,
                                                         @RequestBody PaymentCancelRequest cancelRequest) {

        TossPaymentResponse tossPaymentResponse = paymentService.cancelPayment(cancelRequest.paymentKey(), cancelRequest.reason(), cancelRequest.amount());

        Order order = orderService.getOrder(orderId);

        PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.from(order, tossPaymentResponse, tossPaymentResponse.cancels().getLast().cancelAmount());

        PaymentResponse paymentResponse = paymentService.savePayment(paymentCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(paymentResponse);
    }

}
