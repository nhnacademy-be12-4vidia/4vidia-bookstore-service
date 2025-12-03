package com.nhnacademy._vidiabookstoreservice.user.exception;

// TODO 이것도 고민
public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException() {
        super("비밀번호가 일치하지 않습니다.");
    }
}