package com.nhnacademy._vidiabookstoreservice.order.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class OrderItemNotFoundException extends NotFoundException {

    public OrderItemNotFoundException(Long orderItemId) {
        super("ID에 해당하는 주문아이템을 찾을 수 없습니다. ID: %d".formatted(orderItemId));
    }
}
