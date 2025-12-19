package com.nhnacademy._vidiabookstoreservice.book.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class CategoryNotFoundException extends BaseException {

    public CategoryNotFoundException(Long categoryId) {
        super(BookErrorCode.CATEGORY_NOT_FOUND, "카테고리 ID: %d".formatted(categoryId));
    }
}
