package com.nhnacademy._vidiabookstoreservice.book.exception.already;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class BookAuthorAlreadyExistsException extends BaseException {

    public BookAuthorAlreadyExistsException(String bookTitle, String authorName) {
        super(BookErrorCode.BOOK_AUTHOR_ALREADY_EXISTS, "(도서: %s, 작가: %s)".formatted(bookTitle, authorName));
    }
}
