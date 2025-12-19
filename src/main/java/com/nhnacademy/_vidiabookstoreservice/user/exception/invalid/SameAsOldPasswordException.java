package com.nhnacademy._vidiabookstoreservice.user.exception.invalid;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserErrorCode;

public class SameAsOldPasswordException extends BaseException {
    public SameAsOldPasswordException() {
        super(UserErrorCode.SAME_PASSWORD);
    }
}
