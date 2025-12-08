package com.nhnacademy._vidiabookstoreservice.user.exception;

public class InvalidAuthCodeException extends RuntimeException {
    public InvalidAuthCodeException(String message) {
        super(message);
    }
}
