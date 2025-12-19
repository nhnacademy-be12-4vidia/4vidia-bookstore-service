package com.nhnacademy._vidiabookstoreservice.order.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderErrorCode;

public class PaymentNotFoundException extends BaseException {

    public PaymentNotFoundException(Long orderId) {
        super(OrderErrorCode.PAYMENT_NOT_FOUND, "주문 ID: %d".formatted(orderId));
    }
}
