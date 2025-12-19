package com.nhnacademy._vidiabookstoreservice.user.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class IncorrectPasswordException extends BaseException {
    public IncorrectPasswordException() {
        super(UserErrorCode.INCORRECT_PASSWORD);
    }
}