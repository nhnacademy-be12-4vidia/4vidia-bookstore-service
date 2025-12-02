package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class TagNotFoundException extends NotFoundException {

    public TagNotFoundException(String message) {
        super(message);
    }
}
