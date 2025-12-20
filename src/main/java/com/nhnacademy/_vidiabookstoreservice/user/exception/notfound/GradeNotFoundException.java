package com.nhnacademy._vidiabookstoreservice.user.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserErrorCode;

public class GradeNotFoundException extends BaseException {
    public GradeNotFoundException(Long gradeId) {
        super(UserErrorCode.GRADE_NOT_FOUND, "등급ID: %d".formatted(gradeId));
    }
    public GradeNotFoundException(GradeName gradeName) {
        super(UserErrorCode.GRADE_NOT_FOUND,
                "등급명: %s".formatted(gradeName));
    }
}
