package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.MismatchException;

public class ReviewUserMismatchException extends MismatchException {
    public ReviewUserMismatchException(String message) {
        super(message);
    }
}
