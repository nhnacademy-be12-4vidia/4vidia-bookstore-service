package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class BookReviewSummaryNotFoundException extends NotFoundException {
    public BookReviewSummaryNotFoundException(Long bookId) {
        super("ID에 해당하는 도서리뷰요약을 찾을 수 없습니다. ID: %d".formatted(bookId));
    }
}
