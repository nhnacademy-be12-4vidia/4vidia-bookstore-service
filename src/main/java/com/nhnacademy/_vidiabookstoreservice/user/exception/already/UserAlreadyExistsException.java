package com.nhnacademy._vidiabookstoreservice.user.exception.already;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserErrorCode;

public class UserAlreadyExistsException extends BaseException {
    public UserAlreadyExistsException(String email) {
        super(UserErrorCode.USER_ALREADY_EXISTS, " 이메일: %s".formatted(email));
    }
}
