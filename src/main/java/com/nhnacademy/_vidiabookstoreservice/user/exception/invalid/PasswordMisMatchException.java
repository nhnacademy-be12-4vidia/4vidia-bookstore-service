package com.nhnacademy._vidiabookstoreservice.user.exception.invalid;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserErrorCode;

public class PasswordMisMatchException extends BaseException {
    public PasswordMisMatchException() {
        super(UserErrorCode.PASSWORD_MISMATCH);
    }
}
