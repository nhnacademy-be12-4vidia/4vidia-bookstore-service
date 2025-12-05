package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutListRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.*;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentConfirmRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping//주문화면에 필요한 값
    public ResponseEntity<OrderCheckoutResponse> getOrderCheckout(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
                                                                  @RequestBody OrderCheckoutListRequest orderCheckoutListRequest) {

        OrderCheckoutResponse response = orderService.getOrderCheckoutResponse(xUserId, orderCheckoutListRequest.items());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/create")
    public ResponseEntity<OrderCreateResponse> createOrder(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
                                                           @RequestHeader(value = "X-Guest-Id", required = false) Long xGuestId,
                                                           @RequestBody OrderCreateRequest orderCreateRequest) {
        Long userId = xUserId == null ? xGuestId : xUserId;
        OrderCreateResponse orderId = orderService.saveOrder(userId, orderCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    @PostMapping("/{orderId}/success")
    public ResponseEntity<PaymentResponse> savePaymentDetail(@PathVariable long orderId,
                                                             @RequestBody PaymentConfirmRequest confirmRequest) {
        PaymentResponse paymentResponse = orderService.payAndCompleteOrder(orderId, confirmRequest);

        return ResponseEntity.status(HttpStatus.OK).body(paymentResponse);
    }

    @GetMapping("/{orderId}") //주문내역 상세보기
    public ResponseEntity<OrderResponse> getOrder(@PathVariable long orderId) {
        OrderResponse orderResponse = orderService.getOrderResponse(orderId);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

}
