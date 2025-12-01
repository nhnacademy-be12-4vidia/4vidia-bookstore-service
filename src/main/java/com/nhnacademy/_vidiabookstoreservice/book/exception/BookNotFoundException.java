package com.nhnacademy._vidiabookstoreservice.book.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String message) {
        super(message);
    }
}
