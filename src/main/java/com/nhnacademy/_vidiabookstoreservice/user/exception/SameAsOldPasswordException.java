package com.nhnacademy._vidiabookstoreservice.user.exception;

public class SameAsOldPasswordException extends RuntimeException {
    public SameAsOldPasswordException(String message) {
        super(message);
    }
}
