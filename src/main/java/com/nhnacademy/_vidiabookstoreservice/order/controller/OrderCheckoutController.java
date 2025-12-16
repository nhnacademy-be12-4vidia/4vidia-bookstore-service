package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutListRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderCheckoutResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderCheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderCheckoutController {

    private final OrderCheckoutService orderCheckoutService;

    /**
     * 주문 화면에 보여줄 값
     * @param key : 레디스에서 꺼낼 키값
     * @return 주문화면에 필요한 dto
     */
    @GetMapping // 프론트가 백엔드에 종속되는 단점 존재
    public ResponseEntity<OrderCheckoutResponse> getOrderCheckout(@RequestParam String key) {

        Long userId = UserContext.get().getUserId();
        OrderCheckoutResponse response = orderCheckoutService.getOrderCheckoutResponse(userId, key);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

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
}
