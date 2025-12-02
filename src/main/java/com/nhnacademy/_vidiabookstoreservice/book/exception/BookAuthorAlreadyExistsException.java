package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class BookAuthorAlreadyExistsException extends AlreadyExistsException {

    public BookAuthorAlreadyExistsException(String message) {
        super(message);
    }
}
