package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class ReviewUserMismatchException extends BaseException {
    public ReviewUserMismatchException(Long userId, Long orderedUserId) {
        super(BookErrorCode.REVIEW_USER_MISMATCH, "작성자 아이디 : %d, 주문자 아이디 : %d".formatted(userId, orderedUserId));
    }
}
