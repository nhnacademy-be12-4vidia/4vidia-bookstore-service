package com.nhnacademy._vidiabookstoreservice.order.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class OrderAccessDeniedException extends BaseException {
    public OrderAccessDeniedException(Long orderId, String message) {
        super(OrderErrorCode.ORDER_USER_MISMATCH, "주문아이디 : %s, %s".formatted(orderId, message));
    }
}
