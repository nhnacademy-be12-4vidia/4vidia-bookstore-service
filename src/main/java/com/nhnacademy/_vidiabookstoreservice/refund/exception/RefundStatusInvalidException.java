package com.nhnacademy._vidiabookstoreservice.refund.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class RefundStatusInvalidException extends BaseException {
    public RefundStatusInvalidException() {
        super(RefundErrorCode.REFUND_STATUS_INVALID);
    }
}
