package com.nhnacademy._vidiabookstoreservice.book.exception.already;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class AuthorAlreadyExistsException extends BaseException {

    public AuthorAlreadyExistsException(String authorName) {
        super(BookErrorCode.AUTHOR_ALREADY_EXISTS, "이름: %s".formatted(authorName));
    }
}
