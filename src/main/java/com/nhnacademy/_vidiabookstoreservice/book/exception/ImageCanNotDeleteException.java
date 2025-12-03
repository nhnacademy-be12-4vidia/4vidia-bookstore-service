package com.nhnacademy._vidiabookstoreservice.book.exception;

public class ImageCanNotDeleteException extends RuntimeException {

    public ImageCanNotDeleteException() {
        super("이미지 삭제 중 오류가 발생했습니다.");
    }
}
