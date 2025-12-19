package com.nhnacademy._vidiabookstoreservice.book.exception.already;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class ImageAlreadyExistsException extends BaseException {

    public ImageAlreadyExistsException(String bookTitle, String imgUrl) {
        super(BookErrorCode.IMAGE_ALREADY_EXISTS, "도서: %s, Url: %s".formatted(bookTitle, imgUrl));
    }

    public ImageAlreadyExistsException(Long reviewId, String imgUrl) {
        super(BookErrorCode.IMAGE_ALREADY_EXISTS, "리뷰: %d, Url: %s".formatted(reviewId, imgUrl));
    }
}