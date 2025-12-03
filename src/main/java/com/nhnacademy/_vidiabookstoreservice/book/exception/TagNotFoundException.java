package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class TagNotFoundException extends NotFoundException {

    public TagNotFoundException(Long tagId) {
        super("해당하는 아이디의 태그는 존재하지 않습니다. ID: %d".formatted(tagId));
    }
}
