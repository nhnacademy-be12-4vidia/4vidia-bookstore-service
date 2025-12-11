package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.OrderStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutListRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderTrackingRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.*;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentConfirmRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                                                           @RequestBody OrderCreateRequest orderCreateRequest) {
         //ArgumentResolver찾아보기
        OrderCreateResponse orderId = orderService.saveOrder(xUserId, orderCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    @PostMapping("/{orderId}/success")
    public ResponseEntity<PaymentResponse> savePaymentDetail(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
                                                             @RequestHeader(value = "X-Guest-Id", required = false) Long xGuestId,
                                                             @PathVariable long orderId,
                                                             @RequestBody PaymentConfirmRequest confirmRequest) {
        Long userId = xUserId == null ? xGuestId : xUserId; //비회원이면 장바구니 때문에 필요함
        PaymentResponse paymentResponse = orderService.payAndCompleteOrder(orderId, confirmRequest, userId);

        return ResponseEntity.status(HttpStatus.OK).body(paymentResponse);
    }

    /**
     * 주문 내역 상세보기 OrderDetail
     * @param orderId : 주문아이디
     * @return 주문내역 상세보기
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable long orderId) {
        OrderResponse orderResponse = orderService.getOrderResponse(orderId);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    /**
     * 비회원 주문 상세 내역 보기
     * @param orderTrackingRequest
     * @return
     */
    @PostMapping("/guest")
    public ResponseEntity<OrderResponse> getOrder(@RequestBody OrderTrackingRequest orderTrackingRequest) {
        if (orderService.validateGuest(orderTrackingRequest)) {
            OrderResponse orderResponse = orderService.getOrderResponse(orderTrackingRequest.orderId());
            return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }


    /**
     * 배송 전 주문취소
     * @param orderId : 주문아이디
     * @return 200 OK
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable long orderId) {
        orderService.cancelOrder(orderId);

        return ResponseEntity.ok().build();
    }


    /**
     * 유저가 구매확정 눌렀을 때:  주문아이템 상태변경
     * @param orderId : 주문아이디
     */
    @PutMapping("/confirm-order")
    public void changeConfirmOrder(@RequestBody Long orderId) {
        orderService.changeOrderStatus(orderId, ConfirmStatus.CONFIRMED);
    }

}
