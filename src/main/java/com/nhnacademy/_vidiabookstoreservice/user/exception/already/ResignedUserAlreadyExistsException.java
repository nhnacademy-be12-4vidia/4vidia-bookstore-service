package com.nhnacademy._vidiabookstoreservice.user.exception.already;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserErrorCode;

public class ResignedUserAlreadyExistsException extends BaseException {
    public ResignedUserAlreadyExistsException(String email) {
        super(UserErrorCode.RESIGNED_USER_ALREADY_EXISTS, " email : %s".formatted(email));
    }
}
