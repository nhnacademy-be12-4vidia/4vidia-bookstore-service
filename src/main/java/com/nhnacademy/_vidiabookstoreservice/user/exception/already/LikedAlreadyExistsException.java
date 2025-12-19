package com.nhnacademy._vidiabookstoreservice.user.exception.already;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserErrorCode;

public class LikedAlreadyExistsException extends BaseException {
    public LikedAlreadyExistsException() {
        super(UserErrorCode.LIKED_ALREADY_EXISTS);
    }
}
