package com.nhnacademy._vidiabookstoreservice.book.exception;

public class AuthorIdNotFoundException extends RuntimeException {

    public AuthorIdNotFoundException(String message) {
        super(message);
    }
}
