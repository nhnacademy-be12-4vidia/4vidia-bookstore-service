package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderItemController {
    private final OrderItemService orderItemService;

    @PostMapping("/confirm-item")
    public void changeStatusOrderItem(@RequestBody Long orderItemId) {
        orderItemService.changeStatusOrderItem(orderItemId, ConfirmStatus.CONFIRMED);
    }

}
