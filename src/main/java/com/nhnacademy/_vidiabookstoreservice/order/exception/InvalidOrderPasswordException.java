package com.nhnacademy._vidiabookstoreservice.order.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class InvalidOrderPasswordException extends BaseException {
    public InvalidOrderPasswordException(Long orderId) {
        super(OrderErrorCode.ORDER_PASSWORD_MISMATCH, "주문아이디 : %s".formatted(orderId));
    }
}
