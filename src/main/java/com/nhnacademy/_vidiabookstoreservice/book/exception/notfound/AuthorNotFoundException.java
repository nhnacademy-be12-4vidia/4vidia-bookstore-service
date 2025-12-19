package com.nhnacademy._vidiabookstoreservice.book.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class AuthorNotFoundException extends BaseException {

    public AuthorNotFoundException(String authorName) {
        super(BookErrorCode.AUTHOR_NOT_FOUND, "이름: %s".formatted(authorName));
    }

    public AuthorNotFoundException(Long authorId){
        super(BookErrorCode.AUTHOR_NOT_FOUND, "작가ID: %d".formatted(authorId));
    }
}
