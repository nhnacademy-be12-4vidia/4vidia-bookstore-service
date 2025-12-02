package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class BookAlreadyExistsException extends AlreadyExistsException {

    public BookAlreadyExistsException(String isbn) {
        super("이미 존재하는 도서입니다. ISBN : %s".formatted(isbn));
    }
}
