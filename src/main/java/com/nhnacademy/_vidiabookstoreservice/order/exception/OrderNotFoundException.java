package com.nhnacademy._vidiabookstoreservice.order.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class OrderNotFoundException extends NotFoundException {

    public OrderNotFoundException(Long orderId) {
        super("ID에 해당하는 주문내역을 찾을 수 없습니다. ID: %d".formatted(orderId));
    }
}
