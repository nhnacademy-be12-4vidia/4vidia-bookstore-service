package com.nhnacademy._vidiabookstoreservice.user.exception.invalid;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserErrorCode;

public class InvalidAuthCodeException extends BaseException {
    public InvalidAuthCodeException() {
        super(UserErrorCode.INVALID_AUTH_CODE);
    }
}
