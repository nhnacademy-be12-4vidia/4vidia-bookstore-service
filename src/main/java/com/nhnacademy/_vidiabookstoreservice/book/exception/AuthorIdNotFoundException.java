package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class AuthorIdNotFoundException extends NotFoundException {

    public AuthorIdNotFoundException(Long authorId) {
        super("아이디에 해당하는 작가를 찾을 수 없습니다. 작가ID: %d".formatted(authorId));
    }
}
