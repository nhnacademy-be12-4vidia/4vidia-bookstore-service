package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.RequiredException;

public class BookAuthorRequiredException extends RequiredException {

    public BookAuthorRequiredException(String message) {
        super(message);
    }
}
