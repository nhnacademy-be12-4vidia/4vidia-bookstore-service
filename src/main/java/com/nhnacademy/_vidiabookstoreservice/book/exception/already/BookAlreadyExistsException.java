package com.nhnacademy._vidiabookstoreservice.book.exception.already;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class BookAlreadyExistsException extends BaseException {

    public BookAlreadyExistsException(String isbn) {
        super(BookErrorCode.BOOK_ALREADY_EXISTS, "ISBN : %s".formatted(isbn));
    }
}
