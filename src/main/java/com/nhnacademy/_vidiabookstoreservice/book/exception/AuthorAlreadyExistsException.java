package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class AuthorAlreadyExistsException extends AlreadyExistsException {

    public AuthorAlreadyExistsException(String message) {
        super(message);
    }
}
