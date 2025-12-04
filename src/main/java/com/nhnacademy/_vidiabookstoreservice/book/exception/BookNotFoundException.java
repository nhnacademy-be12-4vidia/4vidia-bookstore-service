package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class BookNotFoundException extends NotFoundException {

    public BookNotFoundException(Long bookId) {
        super("ID에 해당하는 도서를 찾을 수 없습니다. ID: %d".formatted(bookId));
    }

    public BookNotFoundException() {
        super("IDs에 해당하는 도서 리스트를 찾을 수 없습니다.");
    }

}
