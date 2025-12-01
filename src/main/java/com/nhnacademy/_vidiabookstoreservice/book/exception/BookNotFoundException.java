package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class BookNotFoundException extends NotFoundException {

    public BookNotFoundException(String message) {
        super(message);
    }
}
