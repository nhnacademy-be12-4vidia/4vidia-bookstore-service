package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class AuthorNameNotFoundException extends NotFoundException {

    public AuthorNameNotFoundException(String message) {
        super(message);
    }
}
