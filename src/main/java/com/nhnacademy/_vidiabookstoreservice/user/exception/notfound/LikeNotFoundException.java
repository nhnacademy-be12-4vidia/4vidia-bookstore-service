package com.nhnacademy._vidiabookstoreservice.user.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserErrorCode;

public class LikeNotFoundException extends BaseException {
    public LikeNotFoundException() {
        super(UserErrorCode.LIKE_NOT_FOUND);
    }
}
