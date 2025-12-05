package com.nhnacademy._vidiabookstoreservice.order.dto.order.request;

import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;

public record OrderItemRequest(
        long bookId,
        int quantity,
        int salePrice
) {
    public static OrderItemRequest from(OrderCreateRequest.ItemRequestDto itemRequestDto) {
        return new OrderItemRequest(
                itemRequestDto.bookId(),
                itemRequestDto.quantity(),
                itemRequestDto.salePrice()
        );
    }

    public static OrderItemRequest fromOrder(OrderItem orderItem) {
        return new OrderItemRequest(
                orderItem.getBook().getId(),
                orderItem.getQuantity(),
                orderItem.getSalePrice()
        );
    }
}
