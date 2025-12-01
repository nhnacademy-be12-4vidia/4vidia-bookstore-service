package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class BookTagAlreadyExistsException extends AlreadyExistsException {

    public BookTagAlreadyExistsException(String message) {
        super(message);
    }
}
