package com.nhnacademy._vidiabookstoreservice.refund.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class RefundNotAvailableException extends BaseException {
    public RefundNotAvailableException() {
        super(RefundErrorCode.REFUND_NOT_AVAILABLE);
    }
}
