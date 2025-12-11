package com.nhnacademy._vidiabookstoreservice.refund.dto;

import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;

public record OrderItemResponse(
        Long orderItemId,
        Long bookId,
        String bookTitle,
        Integer quantity
) {
    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(orderItem.getOrderItemId(),
                orderItem.getBook().getId(),
                orderItem.getBook().getTitle(),
                orderItem.getQuantity()
        );
    }
}
