package com.nhnacademy._vidiabookstoreservice.user.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class EmailVerificationExpiredException extends BaseException {
    public EmailVerificationExpiredException() {
        super(UserErrorCode.EMAIL_VERIFICATION_REQUIRED);
    }
}
