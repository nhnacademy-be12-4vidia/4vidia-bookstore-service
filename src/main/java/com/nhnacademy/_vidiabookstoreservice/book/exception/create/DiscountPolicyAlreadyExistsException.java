package com.nhnacademy._vidiabookstoreservice.book.exception.create;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class DiscountPolicyAlreadyExistsException extends BaseException {
    public DiscountPolicyAlreadyExistsException() {
        super(BookErrorCode.DISCOUNT_POLICY_ALREADY_EXISTS);
    }
}
