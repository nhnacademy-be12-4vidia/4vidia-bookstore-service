package com.nhnacademy._vidiabookstoreservice.order.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderErrorCode;

public class OrderBookNotFoundException extends BaseException {
    public OrderBookNotFoundException() {
        super(OrderErrorCode.ORDER_BOOK_NOT_FOUND);
    }
}
