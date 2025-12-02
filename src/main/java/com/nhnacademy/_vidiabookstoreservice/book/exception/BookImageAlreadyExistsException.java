package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class BookImageAlreadyExistsException extends AlreadyExistsException {

    public BookImageAlreadyExistsException(String bookTitle, String imgUrl) {
        super("해당 Url은 이미 저장되어있습니다. 도서: %s, Url: %s".formatted(bookTitle, imgUrl));
    }
}
