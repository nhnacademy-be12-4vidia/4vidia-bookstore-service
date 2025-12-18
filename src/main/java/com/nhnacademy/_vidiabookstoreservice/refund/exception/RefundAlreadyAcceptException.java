package com.nhnacademy._vidiabookstoreservice.refund.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class RefundAlreadyAcceptException extends AlreadyExistsException {
    public RefundAlreadyAcceptException() {
        super("이미 승인된 반품입니다.");
    }
}
