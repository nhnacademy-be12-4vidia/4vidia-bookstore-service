package com.nhnacademy._vidiabookstoreservice.user.exception;

public class PasswordNotMatchException extends RuntimeException {
    public PasswordNotMatchException() {
        super("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
    }
}
