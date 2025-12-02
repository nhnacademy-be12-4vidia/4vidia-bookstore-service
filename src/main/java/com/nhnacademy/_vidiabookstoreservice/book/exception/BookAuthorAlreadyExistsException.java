package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class BookAuthorAlreadyExistsException extends AlreadyExistsException {

    public BookAuthorAlreadyExistsException(String bookTitle, String authorName) {
        super("이미 해당 도서에 등록된 작가입니다. (도서: %s, 작가: %s)".formatted(bookTitle, authorName));
    }
}
