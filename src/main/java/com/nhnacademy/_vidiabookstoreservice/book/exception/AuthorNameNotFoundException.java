package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class AuthorNameNotFoundException extends NotFoundException {

    public AuthorNameNotFoundException(String authorName) {
        super("해당하는 이름의 작가를 찾을 수 없습니다. 이름: %s".formatted(authorName));
    }
}
