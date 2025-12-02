package com.nhnacademy._vidiabookstoreservice.user.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class AlreadyLikedException extends AlreadyExistsException {
    public AlreadyLikedException() {
        super("이미 좋아요가 되어있습니다.");
    }
}
