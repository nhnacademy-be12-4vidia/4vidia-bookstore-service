package com.nhnacademy._vidiabookstoreservice.user.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class EmailSendFailedException extends BaseException {

    public EmailSendFailedException() {
        super(UserErrorCode.EMAIL_SEND_FAILED);
    }

    public EmailSendFailedException(Throwable cause) {
        super(UserErrorCode.EMAIL_SEND_FAILED);
        initCause(cause);
    }
}
