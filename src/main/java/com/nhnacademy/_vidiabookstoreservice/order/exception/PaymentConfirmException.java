package com.nhnacademy._vidiabookstoreservice.order.exception;
// 500?
public class PaymentConfirmException extends RuntimeException {
    public PaymentConfirmException(String message) {
        super(message);
    }
}
