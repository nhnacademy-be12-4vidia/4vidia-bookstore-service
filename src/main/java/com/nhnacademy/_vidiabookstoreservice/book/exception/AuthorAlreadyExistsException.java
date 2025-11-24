package com.nhnacademy._vidiabookstoreservice.book.exception;

public class AuthorAlreadyExistsException extends RuntimeException {

    public AuthorAlreadyExistsException(String message) {
        super(message);
    }
}
