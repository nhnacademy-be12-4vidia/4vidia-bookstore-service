package com.nhnacademy._vidiabookstoreservice.book.exception.already;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class CategoryAlreadyExistsException extends BaseException {
    public CategoryAlreadyExistsException(String kdcCode) {
        super(BookErrorCode.CATEGORY_ALREADY_EXISTS, "이미 존재하는 카테고리입니다. Code: " + kdcCode);
    }
}
