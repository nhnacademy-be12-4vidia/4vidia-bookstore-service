package com.nhnacademy._vidiabookstoreservice.user.exception;

public class AuthCodeExpiredException extends RuntimeException {
    public AuthCodeExpiredException(String message) {
        super(message);
    }
}
