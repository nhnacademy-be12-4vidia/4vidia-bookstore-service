package com.nhnacademy._vidiabookstoreservice.admin.exception;

public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
}
