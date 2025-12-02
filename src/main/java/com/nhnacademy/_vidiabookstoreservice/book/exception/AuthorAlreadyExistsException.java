package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class AuthorAlreadyExistsException extends AlreadyExistsException {

    public AuthorAlreadyExistsException(String authorName) {
        super("해당하는 이름의 작가가 이미 존재합니다. 이름: %s".formatted(authorName));
    }
}
