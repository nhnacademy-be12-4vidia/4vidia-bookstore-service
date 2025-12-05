package com.nhnacademy._vidiabookstoreservice.user.exception;

public class AlreadyResignedUserException extends RuntimeException {
    public AlreadyResignedUserException(String email) {
        super("이미 탈퇴한 회원입니다. email : %s".formatted(email));
    }
}
