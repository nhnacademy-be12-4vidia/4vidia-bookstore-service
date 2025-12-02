package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class CategoryNotFoundException extends NotFoundException {

    public CategoryNotFoundException(Long categoryId) {
        super("해당하는 아이디의 카테고리는 존재하지 않습니다. ID: %d".formatted(categoryId));
    }
}
