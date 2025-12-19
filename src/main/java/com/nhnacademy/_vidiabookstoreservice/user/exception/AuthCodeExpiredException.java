package com.nhnacademy._vidiabookstoreservice.user.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class AuthCodeExpiredException extends BaseException {
    public AuthCodeExpiredException() {
        super(UserErrorCode.AUTH_CODE_EXPIRED);
    }
}
