package com.nhnacademy._vidiabookstoreservice.user.exception;

public class GradeNotFoundException extends RuntimeException {
    public GradeNotFoundException(Long gradeId) {
        super("존재하지 않는 등급입니다. %d".formatted(gradeId));
    }
}
