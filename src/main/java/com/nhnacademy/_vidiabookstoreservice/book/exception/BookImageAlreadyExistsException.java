package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class BookImageAlreadyExistsException extends AlreadyExistsException {

    public BookImageAlreadyExistsException(String message) {
        super(message);
    }
}
