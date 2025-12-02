package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class BookAlreadyExistsException extends AlreadyExistsException {

    public BookAlreadyExistsException(String message) {
        super(message);
    }
}
