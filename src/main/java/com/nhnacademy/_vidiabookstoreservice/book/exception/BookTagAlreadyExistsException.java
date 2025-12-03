package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class BookTagAlreadyExistsException extends AlreadyExistsException {

    public BookTagAlreadyExistsException(String bookTitle, String bookTag) {
        super("이미 해당 도서에 등록된 태그입니다. (도서: %s, 태그: %s)".formatted(bookTitle, bookTag));
    }
}
