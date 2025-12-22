package com.nhnacademy._vidiabookstoreservice.refund.exception.already;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.refund.exception.RefundErrorCode;

public class RefundAlreadyApprovedException extends BaseException {
    public RefundAlreadyApprovedException() {
        super(RefundErrorCode.REFUND_ALREADY_APPROVED);
    }
}
