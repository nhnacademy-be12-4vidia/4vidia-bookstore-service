package com.nhnacademy._vidiabookstoreservice.user.exception.invalid;

public class InvalidAuthCodeException extends RuntimeException {
    public InvalidAuthCodeException(String message) {
        super(message);
    }
}
