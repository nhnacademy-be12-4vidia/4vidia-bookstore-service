package com.nhnacademy._vidiabookstoreservice.book.exception;

public class AuthorNameNotFoundException extends RuntimeException {

    public AuthorNameNotFoundException(String message) {
        super(message);
    }
}
