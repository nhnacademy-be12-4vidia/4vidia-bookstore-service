package com.nhnacademy._vidiabookstoreservice.refund.exception.already;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.refund.exception.RefundErrorCode;

public class DamageRefundNotAvailableException extends BaseException {
    public DamageRefundNotAvailableException() {
        super(RefundErrorCode.DAMAGE_REFUND_NOT_AVAILABLE);
    }
}
