package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class TagNameAlreadyExistsException extends AlreadyExistsException {

    public TagNameAlreadyExistsException(String tagName) {
        super("이미 존재하는 태그입니다. 태그 이름: %s".formatted(tagName));
    }
}
