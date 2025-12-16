package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderPreviewResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.OrderUserResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users/me") // 기존 /my/orders
public class MyOrderController {
    private final OrderService orderService;
    private final UserService userService;

    /**
     * 주문내역 미리보기
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderPreviewResponse>> getOrderPreview() {
        Long userId = UserContext.get().getUserId();

        List<OrderPreviewResponse> orderPreviewResponse = orderService.getOrdersByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(orderPreviewResponse);
    }

    /**
     * 주문 화면에 필요한 유저 정보
     * @return : 유저 정보, 배송지리스트, 기본배송지, 보유 포인트
     */
    @GetMapping("/order-info")
    public ResponseEntity<OrderUserResponse> getOrderUserCheckout() {
        Long userId = UserContext.get().getUserId();

        OrderUserResponse orderUserResponse = userService.getOrderUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(orderUserResponse);
    }

}
