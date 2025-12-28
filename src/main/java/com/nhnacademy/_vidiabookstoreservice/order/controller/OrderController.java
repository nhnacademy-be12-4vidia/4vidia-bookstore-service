package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutListRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderTrackingRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.*;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentConfirmRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderCheckoutService;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성 및 저장
     * @param orderCreateRequest : 주문화면에서 넘어온
     * @return 주문아이디 DTO
     */
    @PostMapping // 주문 생성 및 저장
    public ResponseEntity<OrderCreateResponse> createOrder(@RequestBody @Valid OrderCreateRequest orderCreateRequest) {
        Long userId = UserContext.get().getUserId();
        OrderCreateResponse orderId = orderService.saveOrder(userId, orderCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    /**
     * 회원 주문 내역 상세보기 OrderDetail
     * @param orderId : 주문아이디
     * @return 주문내역 상세보기
     */
    @GetMapping("/{order-id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable(value = "order-id") long orderId) {
        Long userId = UserContext.get().getUserId();
        OrderResponse orderResponse = orderService.getOrderResponse(userId, orderId);

        return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
    }

    @GetMapping("/{order-id}/amount")
    public ResponseEntity<OrderAmountResponse> getOrderPayPrice(@PathVariable(value = "order-id") long orderId) {
        OrderAmountResponse orderAmountResponse = orderService.getOrderPayPrice(orderId);

        return ResponseEntity.status(HttpStatus.OK).body(orderAmountResponse);
    }

    /**
     * 비회원 주문 상세 내역 보기
     * @param orderTrackingRequest : 주문아이디, 주문비밀번호
     * @return 성공시 주문내역 상세보기, 실패시 null 값 반환
     */
    @PostMapping("/guest") // api 매핑 고민
    public ResponseEntity<OrderResponse> getOrder(@RequestBody OrderTrackingRequest orderTrackingRequest) {
        OrderResponse orderResponse = orderService.getGuestOrderResponse(orderTrackingRequest);

        return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
    }

    /**
     * 배송 전 주문취소
     * @param orderId : 주문아이디
     * @return 200 OK
     */
    @PutMapping("/{order-id}/cancel")
    public void cancelOrder(@PathVariable(value = "order-id") long orderId) {
        orderService.cancelOrder(orderId, "배송 전 취소");
    }

    /**
     * 유저 구매확정 눌렀을 때:  주문아이템 상태변경
     * @param orderId : 주문아이디
     */
    @PutMapping("/{order-id}/confirm-order")
    public void changeConfirmOrder(@PathVariable("order-id") Long orderId) {
        orderService.changeOrderStatus_ByUser(orderId, ConfirmStatus.CONFIRMED);

    }

}
