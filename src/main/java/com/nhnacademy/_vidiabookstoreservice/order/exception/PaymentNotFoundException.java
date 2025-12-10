package com.nhnacademy._vidiabookstoreservice.order.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class PaymentNotFoundException extends NotFoundException {

    public PaymentNotFoundException(Long orderId) {
        super("ID에 해당하는 결제내역을 찾을 수 없습니다. ID: %d".formatted(orderId));
    }
}
