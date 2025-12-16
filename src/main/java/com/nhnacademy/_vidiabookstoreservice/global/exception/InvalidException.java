package com.nhnacademy._vidiabookstoreservice.global.exception;

/**
 * 비즈니스 로직 상 "유효하지 않은 상태"
 * 입력값 자체가 문법적으로 틀리거나 Null 같은 검증 오류가 아니라, 값은 존재하지만 비즈니스 규칙에 맞지 않아 처리가 불가능한 상태
 */
public class InvalidException extends RuntimeException {
    public InvalidException(String message) {
        super(message);
    }
}
