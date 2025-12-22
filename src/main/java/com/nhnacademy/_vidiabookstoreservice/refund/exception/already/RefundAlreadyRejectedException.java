package com.nhnacademy._vidiabookstoreservice.refund.exception.already;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.refund.exception.RefundErrorCode;

public class RefundAlreadyRejectedException extends BaseException {
    public RefundAlreadyRejectedException() {
        super(RefundErrorCode.REFUND_ALREADY_REJECTED);
    }
}
