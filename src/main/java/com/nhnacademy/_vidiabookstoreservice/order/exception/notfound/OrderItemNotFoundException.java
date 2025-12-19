package com.nhnacademy._vidiabookstoreservice.order.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderErrorCode;

public class OrderItemNotFoundException extends BaseException {

    public OrderItemNotFoundException(Long orderItemId) {
        super(OrderErrorCode.ORDER_ITEM_NOT_FOUND, "주문아이템 ID: %d".formatted(orderItemId));
    }
}
