package com.nhnacademy._vidiabookstoreservice.admin.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class ReviewNotFoundException extends NotFoundException {
    public ReviewNotFoundException(Long reviewId) {
        super("리뷰를 찾을 수 없습니다. 리뷰 아이디: %d".formatted(reviewId));
    }
}
