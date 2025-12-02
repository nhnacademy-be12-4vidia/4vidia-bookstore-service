package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.MismatchException;

public class ReviewUserMismatchException extends MismatchException {
    public ReviewUserMismatchException(Long userId, Long orderedUserId) {
        super("해당 상품을 주문한 사용자만 리뷰를 작성할 수 있습니다. 작성자 아이디 : %d, 주문자 아이디 : %d".formatted(userId,
                orderedUserId));
    }
}
