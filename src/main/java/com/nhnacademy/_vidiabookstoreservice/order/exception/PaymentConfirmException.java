package com.nhnacademy._vidiabookstoreservice.order.exception;

// TODO 어떤 exception을 상속해야 하는지.. 그냥 Runtime?
public class PaymentConfirmException extends RuntimeException {
    public PaymentConfirmException(String message) {
        super(message);
    }
}
