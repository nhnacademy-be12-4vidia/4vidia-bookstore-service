package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.DeliveryResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminDeliveryService;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/deliveries")
@RequiredArgsConstructor
public class AdminDeliveryController {
    private final AdminDeliveryService adminDeliveryService;
    private final OrderService orderService;

    @GetMapping
    public Page<DeliveryResponse> listByDeliveryStatus(
            @RequestParam(value = "deliveryStatus", required = false) DeliveryStatus deliveryStatus,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return adminDeliveryService.listByDeliveryStatus(deliveryStatus, pageable)
                .map(DeliveryResponse::from);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<DeliveryResponse> getOrderDetail(@PathVariable long orderId){
        Order o = orderService.getOrder(orderId);
        return ResponseEntity.ok().body(DeliveryResponse.from(o));
    }

    @PutMapping("/{orderId}/start-delivery")
    public ResponseEntity<DeliveryResponse> startDelivery(@PathVariable Long orderId) {
        Order o = adminDeliveryService.startDelivery(orderId);
        return ResponseEntity.ok().body(DeliveryResponse.from(o));
    }

    @PutMapping("/{orderId}/complete-delivery")
    public ResponseEntity<DeliveryResponse> completeDelivery(@PathVariable Long orderId) {
        Order o = adminDeliveryService.completeDelivery(orderId);
        return ResponseEntity.ok().body(DeliveryResponse.from(o));
    }
}
