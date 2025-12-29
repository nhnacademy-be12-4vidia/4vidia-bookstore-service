package com.nhnacademy._vidiabookstoreservice.book.exception.delete;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class CategoryCannotDeleteException extends BaseException {

    public CategoryCannotDeleteException(String reason) {
        super(BookErrorCode.CATEGORY_CANNOT_DELETE, reason);
    }
}
