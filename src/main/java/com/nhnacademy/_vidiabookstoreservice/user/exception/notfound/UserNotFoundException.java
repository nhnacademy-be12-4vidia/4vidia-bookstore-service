package com.nhnacademy._vidiabookstoreservice.user.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserErrorCode;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException() {
        super(UserErrorCode.USER_NOT_FOUND);
    }

    public UserNotFoundException(Long userId){
        super(UserErrorCode.USER_NOT_FOUND, " 회원 아이디 : %d".formatted(userId));
    }

    public UserNotFoundException(String email){
        super(UserErrorCode.USER_NOT_FOUND, " 이메일 : %s".formatted(email));
    }
}
