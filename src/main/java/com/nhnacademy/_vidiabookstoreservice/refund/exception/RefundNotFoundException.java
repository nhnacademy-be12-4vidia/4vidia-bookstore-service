package com.nhnacademy._vidiabookstoreservice.refund.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class RefundNotFoundException extends BaseException {
    public RefundNotFoundException(Long refundId) {
        super(RefundErrorCode.REFUND_NOT_FOUND, "RefundId : " + refundId);
    }
}
