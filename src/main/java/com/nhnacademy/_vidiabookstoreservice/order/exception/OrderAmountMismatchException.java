package com.nhnacademy._vidiabookstoreservice.order.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class OrderAmountMismatchException extends BaseException {
    public OrderAmountMismatchException() {
        super(OrderErrorCode.ORDER_AMOUNT_MISMATCH);
    }
}
