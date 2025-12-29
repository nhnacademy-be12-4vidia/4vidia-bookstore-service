package com.nhnacademy._vidiabookstoreservice.book.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class DiscountPolicyNotFoundException extends BaseException {

    public DiscountPolicyNotFoundException(Long id) {
        super(BookErrorCode.DISCOUNT_POLICY_NOT_FOUND, "할인 정책 ID: %d".formatted(id));
    }
}
