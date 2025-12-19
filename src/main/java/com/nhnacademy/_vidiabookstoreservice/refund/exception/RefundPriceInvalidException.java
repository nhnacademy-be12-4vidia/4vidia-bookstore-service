package com.nhnacademy._vidiabookstoreservice.refund.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class RefundPriceInvalidException extends BaseException {
    public RefundPriceInvalidException() {
        super(RefundErrorCode.REFUND_PRICE_INVALID);
    }
}
