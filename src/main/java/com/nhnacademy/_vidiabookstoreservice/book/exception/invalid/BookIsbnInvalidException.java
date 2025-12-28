package com.nhnacademy._vidiabookstoreservice.book.exception.invalid;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class BookIsbnInvalidException extends BaseException {
    public BookIsbnInvalidException() {
        super(BookErrorCode.BOOK_ISBN_INVALID);
    }
}
