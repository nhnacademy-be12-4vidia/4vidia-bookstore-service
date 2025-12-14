package com.nhnacademy._vidiabookstoreservice.order.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.MismatchException;

public class OrderAmountMismatchException extends MismatchException {
    public OrderAmountMismatchException() {
        super("주문 금액에 변화가 있습니다");
    }
}
