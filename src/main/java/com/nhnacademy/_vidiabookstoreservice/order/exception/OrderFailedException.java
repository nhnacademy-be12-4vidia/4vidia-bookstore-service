package com.nhnacademy._vidiabookstoreservice.order.exception;

public class OrderFailedException extends RuntimeException {
    public OrderFailedException(String message) {
        super(message);
    }
}
