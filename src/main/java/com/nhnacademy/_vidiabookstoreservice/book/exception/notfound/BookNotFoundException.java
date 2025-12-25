package com.nhnacademy._vidiabookstoreservice.book.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class BookNotFoundException extends BaseException {

    public BookNotFoundException(Long bookId) {
        super(BookErrorCode.BOOK_NOT_FOUND, "도서 ID: %d".formatted(bookId));
    }

    public BookNotFoundException(String isbn) {
        super(BookErrorCode.BOOK_NOT_FOUND, "ISBN: %s".formatted(isbn));
    }

}
