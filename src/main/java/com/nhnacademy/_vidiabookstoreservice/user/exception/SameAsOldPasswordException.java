package com.nhnacademy._vidiabookstoreservice.user.exception;

// TODO 이것도
public class SameAsOldPasswordException extends RuntimeException {
    public SameAsOldPasswordException() {
        super("현재 비밀번호와 동일한 비밀번호로는 변경할 수 없습니다.");
    }
}
