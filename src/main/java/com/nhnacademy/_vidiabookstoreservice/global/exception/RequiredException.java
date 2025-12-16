package com.nhnacademy._vidiabookstoreservice.global.exception;

/**
 * 사용자가 필수 입력을 빠뜨리거나 누락한 경우
 */
public class RequiredException extends RuntimeException {

    public RequiredException(String message) {
        super(message);
    }
}
