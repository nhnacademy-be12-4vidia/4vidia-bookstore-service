package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.RequiredException;

public class BookAuthorRequiredException extends RequiredException {

    public BookAuthorRequiredException() {
        super("작가는 최소 한 명 이상 필요합니다.");
    }
}
