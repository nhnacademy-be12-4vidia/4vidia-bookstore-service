package com.nhnacademy._vidiabookstoreservice.book.exception.invalid;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class BookAuthorRequiredException extends BaseException {

    public BookAuthorRequiredException() {
        super(BookErrorCode.BOOK_AUTHOR_INVALID);
    }
}
