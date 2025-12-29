package com.nhnacademy._vidiabookstoreservice.order.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderErrorCode;

public class OrderRedisNotFoundException extends BaseException {
    public OrderRedisNotFoundException() {
        super(OrderErrorCode.ORDER_AMOUNT_MISMATCH);
    }
}
