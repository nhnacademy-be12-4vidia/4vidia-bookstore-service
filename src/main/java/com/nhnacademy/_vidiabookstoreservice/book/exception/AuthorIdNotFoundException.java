package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class AuthorIdNotFoundException extends NotFoundException {

    public AuthorIdNotFoundException(String message) {
        super(message);
    }
}
