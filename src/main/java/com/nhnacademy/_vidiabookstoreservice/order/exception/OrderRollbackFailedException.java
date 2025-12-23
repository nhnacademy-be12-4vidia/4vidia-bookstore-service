package com.nhnacademy._vidiabookstoreservice.order.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class OrderRollbackFailedException extends BaseException {
    public OrderRollbackFailedException(String message) {
        super(OrderErrorCode.ORDER_ROLLBACK_FAILED, "message : %s".formatted(message));
    }
}
