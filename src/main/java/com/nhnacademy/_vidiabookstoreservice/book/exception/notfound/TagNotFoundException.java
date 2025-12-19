package com.nhnacademy._vidiabookstoreservice.book.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class TagNotFoundException extends BaseException {

    public TagNotFoundException(Long tagId) {
        super(BookErrorCode.TAG_NOT_FOUND, "태그 ID: %d".formatted(tagId));
    }
}
