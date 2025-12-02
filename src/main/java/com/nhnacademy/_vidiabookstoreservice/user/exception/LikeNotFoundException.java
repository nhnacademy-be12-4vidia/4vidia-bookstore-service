package com.nhnacademy._vidiabookstoreservice.user.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class LikeNotFoundException extends NotFoundException {
    public LikeNotFoundException() {
        super("좋아요를 찾을 수 없습니다.");
    }
}
