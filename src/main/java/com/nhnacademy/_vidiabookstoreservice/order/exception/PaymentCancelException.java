package com.nhnacademy._vidiabookstoreservice.order.exception;

public class PaymentCancelException extends RuntimeException {
    public PaymentCancelException(String message) {
        super(message);
    }
}
