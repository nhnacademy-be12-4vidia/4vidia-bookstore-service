package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class ReviewImageAlreadyExistsException extends AlreadyExistsException {
    public ReviewImageAlreadyExistsException(Long reviewId, String imgUrl) {
        super("해당 이미지 Url은 이미 저장되어있습니다. 리뷰: %d, Url: %s".formatted(reviewId, imgUrl));
    }
}
