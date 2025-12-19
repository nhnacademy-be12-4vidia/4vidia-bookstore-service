package com.nhnacademy._vidiabookstoreservice.refund.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class RefundAlreadyAcceptException extends BaseException {
    public RefundAlreadyAcceptException() {
        super(RefundErrorCode.REFUND_ALREADY_ACCEPT);
    }
}
