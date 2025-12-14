package com.nhnacademy._vidiabookstoreservice.order.controller;

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
    private final OrderCheckoutService orderCheckoutService;

    /**
     * 주문 전 선택된 아이템 Redis에 저장
     * @param orderCheckoutListRequest : 도서 상세/장바구니 에서 선택된 아이템
     * @return 레디스 저장키값 반환
     */
    @PostMapping("/checkout-temp")
    public ResponseEntity<String> createCheckoutSession(@RequestBody OrderCheckoutListRequest orderCheckoutListRequest) {

        String key = orderCheckoutService.initiateCheckout(orderCheckoutListRequest.items());

        return ResponseEntity.status(HttpStatus.CREATED).body(key);
    }

    /**
     * 주문 화면에 보여줄 값
     * @param xUserId : 회원이면 유저아이디, 비회원이면 null 값
     * @param key : 레디스에서 꺼낼 키값
     * @return 주문화면에 필요한 dto
     */
    @GetMapping // 프론트가 백엔드에 종속되는 단점 존재
    public ResponseEntity<OrderCheckoutResponse> getOrderCheckout(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
                                                                  @RequestParam String key) {

        OrderCheckoutResponse response = orderCheckoutService.getOrderCheckoutResponse(xUserId, key);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 주문 생성 및 저장
     * @param xUserId : 회원이면 유저아이디, 비회원이면 null
     * @param orderCreateRequest : 주문화면에서 넘어온
     * @return
     */
    @PostMapping // 주문 생성 및 저장
    public ResponseEntity<OrderCreateResponse> createOrder(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
                                                           @RequestBody @Valid OrderCreateRequest orderCreateRequest) {
         //ArgumentResolver찾아보기
        OrderCreateResponse orderId = orderService.saveOrder(xUserId, orderCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
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
     * @param orderTrackingRequest : 주문아이디, 주문비밀번호
     * @return 성공시 주문내역 상세보기, 실패시 null 값 반환
     */
    @PostMapping("/guest") // 고민
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

        return ResponseEntity.noContent().build();
    }

    /**
     * 유저 구매확정 눌렀을 때:  주문아이템 상태변경
     * @param orderId : 주문아이디
     */
    @PutMapping("/{order-id}/confirm-order")
    public ResponseEntity<Void> changeConfirmOrder(@RequestBody Long orderId) {
        orderService.changeOrderStatus_ByUser(orderId, ConfirmStatus.CONFIRMED);

        return ResponseEntity.noContent().build();
    }

}
