package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class ImageUploadException extends BaseException {
    public ImageUploadException() {
        super(BookErrorCode.IMAGE_UPLOAD_FAIL);
    }
}
