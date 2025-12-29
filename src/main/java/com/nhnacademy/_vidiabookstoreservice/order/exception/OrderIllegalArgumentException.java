package com.nhnacademy._vidiabookstoreservice.order.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class OrderIllegalArgumentException extends BaseException {
    public OrderIllegalArgumentException() {
        super(OrderErrorCode.ORDER_BAD_REQUEST);
    }
}
