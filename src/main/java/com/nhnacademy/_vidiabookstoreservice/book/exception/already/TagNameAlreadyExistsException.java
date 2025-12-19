package com.nhnacademy._vidiabookstoreservice.book.exception.already;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class TagNameAlreadyExistsException extends BaseException {

    public TagNameAlreadyExistsException(String tagName) {
        super(BookErrorCode.TAG_NAME_ALREADY_EXISTS, " 태그 이름: %s".formatted(tagName));
    }
}
