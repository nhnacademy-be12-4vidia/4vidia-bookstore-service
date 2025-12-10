package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderPreviewResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users/me/orders") // 기존 /my/orders
public class MyOrderController {
    private final OrderService orderService;

    /**
     * 주문내역 미리보기
     */
    @GetMapping
    public ResponseEntity<List<OrderPreviewResponse>> getOrderPreview(@RequestHeader(value = "X-User-Id") Long userId) {
        List<OrderPreviewResponse> orderPreviewResponse = orderService.getOrdersByUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderPreviewResponse);
    }

}
