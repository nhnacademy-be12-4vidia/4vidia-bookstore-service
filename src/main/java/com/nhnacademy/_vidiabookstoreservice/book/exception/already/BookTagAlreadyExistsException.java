package com.nhnacademy._vidiabookstoreservice.book.exception.already;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class BookTagAlreadyExistsException extends BaseException {

    public BookTagAlreadyExistsException(String bookTitle, String bookTag) {
        super(BookErrorCode.BOOK_TAG_ALREADY_EXISTS, "(도서: %s, 태그: %s)".formatted(bookTitle, bookTag));
    }
}
