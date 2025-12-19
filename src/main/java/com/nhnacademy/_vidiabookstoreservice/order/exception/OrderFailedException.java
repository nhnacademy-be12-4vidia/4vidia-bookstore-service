package com.nhnacademy._vidiabookstoreservice.order.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class OrderFailedException extends BaseException {
    public OrderFailedException(String message) {
        super(OrderErrorCode.ORDER_FAILED, "message : %s".formatted(message));
    }
}
