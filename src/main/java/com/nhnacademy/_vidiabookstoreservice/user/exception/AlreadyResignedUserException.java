package com.nhnacademy._vidiabookstoreservice.user.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class AlreadyResignedUserException extends AlreadyExistsException {
    public AlreadyResignedUserException(String email) {
        super("이미 탈퇴한 회원입니다. email : %s".formatted(email));
    }
}
