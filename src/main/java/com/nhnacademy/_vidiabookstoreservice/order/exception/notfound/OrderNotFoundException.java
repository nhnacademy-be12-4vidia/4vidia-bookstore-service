package com.nhnacademy._vidiabookstoreservice.order.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderErrorCode;

public class OrderNotFoundException extends BaseException {

    public OrderNotFoundException(Long orderId) {
        super(OrderErrorCode.ORDER_NOT_FOUND, "주문 ID: %d".formatted(orderId));
    }
}
