package com.nhnacademy._vidiabookstoreservice.user.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class UserNotFoundByEmailException extends NotFoundException {
    public UserNotFoundByEmailException(String email) {
        super("존재하지 않는 회원입니다. 이메일 : %s".formatted(email));
    }
}
