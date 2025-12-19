package com.nhnacademy._vidiabookstoreservice.user.exception.already;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserErrorCode;

public class ResignedUserAlreadyExistsException extends BaseException {
    public ResignedUserAlreadyExistsException(String email) {
        super(UserErrorCode.GRADE_RATE_INVALID, " email : %s".formatted(email));
    }
}
