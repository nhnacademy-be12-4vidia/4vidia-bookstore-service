package com.nhnacademy._vidiabookstoreservice.refund.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class RefundItemNotFoundException extends BaseException {
    public RefundItemNotFoundException(Long refundId) {
        super(RefundErrorCode.REFUND_ITEM_NOT_FOUND, "반품 아이디: %d".formatted(refundId));
    }
}
