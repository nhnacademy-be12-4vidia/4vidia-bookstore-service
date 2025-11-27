package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.impl.OrderService;
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
    public ResponseEntity<Long> createOrder(@RequestBody OrderCreateRequest orderCreateRequest) {
        long orderId = orderService.saveOrder(orderCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable long orderId) {
        OrderResponse orderResponse = orderService.getOrderResponse(orderId);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }


}
