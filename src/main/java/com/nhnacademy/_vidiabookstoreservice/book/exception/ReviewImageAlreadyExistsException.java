package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class ReviewImageAlreadyExistsException extends AlreadyExistsException {
    public ReviewImageAlreadyExistsException(String message) {
        super(message);
    }
}
