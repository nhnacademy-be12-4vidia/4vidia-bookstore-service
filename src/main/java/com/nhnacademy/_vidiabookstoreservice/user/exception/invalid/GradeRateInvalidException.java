package com.nhnacademy._vidiabookstoreservice.user.exception.invalid;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserErrorCode;

public class GradeRateInvalidException extends BaseException {
    public GradeRateInvalidException() {
        super(UserErrorCode.GRADE_RATE_INVALID);
    }
}
