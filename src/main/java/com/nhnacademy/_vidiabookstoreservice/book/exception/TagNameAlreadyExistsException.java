package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class TagNameAlreadyExistsException extends AlreadyExistsException {

    public TagNameAlreadyExistsException(String message) {
        super(message);
    }
}
