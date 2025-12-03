package com.nhnacademy._vidiabookstoreservice.user.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException() {
        super("일치하는 회원젇보가 없습니다.");
    }
}
