package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;


import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;

public record OrderItemResponse(
        Long orderItemId,
        Order order,
        Long bookId,
        Integer quantity,
        Integer salePrice,
        ConfirmStatus confirmStatus
) {
    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getOrderItemId(),
                orderItem.getOrder(),
                orderItem.getBook().getId(),
                orderItem.getQuantity(),
                orderItem.getSalePrice(),
                orderItem.getConfirmStatus()
        );
    }
}
