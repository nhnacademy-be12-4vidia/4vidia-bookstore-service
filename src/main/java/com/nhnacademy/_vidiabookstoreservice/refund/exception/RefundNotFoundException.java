package com.nhnacademy._vidiabookstoreservice.refund.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class RefundNotFoundException extends NotFoundException {
    public RefundNotFoundException(Long refundId) {
        super("반품 내역을 찾을 수 없습니다. RefundId : " + refundId);
    }
}
