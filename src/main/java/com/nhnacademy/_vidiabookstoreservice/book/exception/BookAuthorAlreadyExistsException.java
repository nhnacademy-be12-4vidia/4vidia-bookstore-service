package com.nhnacademy._vidiabookstoreservice.book.exception;

public class BookAuthorAlreadyExistsException extends RuntimeException {

    public BookAuthorAlreadyExistsException(String message) {
        super(message);
    }
}
