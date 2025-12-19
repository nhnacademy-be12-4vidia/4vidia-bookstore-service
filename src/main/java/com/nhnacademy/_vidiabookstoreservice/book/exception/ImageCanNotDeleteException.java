package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class ImageCanNotDeleteException extends BaseException {
    public ImageCanNotDeleteException() {
        super(BookErrorCode.IMAGE_DELETE_FAIL);
    }
}
