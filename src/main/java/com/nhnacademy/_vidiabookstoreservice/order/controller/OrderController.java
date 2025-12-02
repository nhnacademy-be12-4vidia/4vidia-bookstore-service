package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.DeliveryDateResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderPreviewResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/delivery-dates")
    public ResponseEntity<List<DeliveryDateResponse>> getDeliveryDate() {
        List<DeliveryDateResponse> deliveryDateResponses = orderService.getDeliveryDates();

        return ResponseEntity.status(HttpStatus.OK).body(deliveryDateResponses);
    }

    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
                                            @RequestHeader(value = "X-Guest-Id", required = false) Long xGuestId,
                                            @RequestBody OrderCreateRequest orderCreateRequest) {
        long userId = xUserId == null ? xGuestId : xUserId;
        long orderId = orderService.saveOrder(userId, orderCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable long orderId) {
        OrderResponse orderResponse = orderService.getOrderResponse(orderId);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<List<OrderPreviewResponse>> getOrderPreview(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
                                                                      @RequestHeader(value = "X-Guest-Id", required = false) Long xGuestId) {
        long userId = xUserId == null ? xGuestId : xUserId;
        List<OrderPreviewResponse> OrderPreviewResponse = orderService.getOrdersByUserId(userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(OrderPreviewResponse);
    }


}
