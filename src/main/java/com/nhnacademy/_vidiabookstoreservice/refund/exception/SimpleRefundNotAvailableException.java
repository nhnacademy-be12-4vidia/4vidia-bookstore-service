package com.nhnacademy._vidiabookstoreservice.refund.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class SimpleRefundNotAvailableException extends BaseException {
    public SimpleRefundNotAvailableException() {
        super(RefundErrorCode.SIMPLE_REFUND_NOT_AVAILABLE);
    }
}
