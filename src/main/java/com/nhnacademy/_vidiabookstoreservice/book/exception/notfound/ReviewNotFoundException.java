package com.nhnacademy._vidiabookstoreservice.book.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class ReviewNotFoundException extends BaseException {
    public ReviewNotFoundException(Long reviewId) {
        super(BookErrorCode.REVIEW_NOT_FOUND, "리뷰 아이디: %d".formatted(reviewId));
    }
}
