package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy.order.domain.dto.OrderCreateRequest;
import com.nhnacademy.order.domain.dto.OrderResponse;
import com.nhnacademy.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@ModelAttribute OrderCreateRequest orderCreateRequest) {
        OrderResponse response = orderService.saveOrder(orderCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable long orderId) {
        OrderResponse orderResponse = orderService.getOrder(orderId);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }


}
