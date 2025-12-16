package com.nhnacademy._vidiabookstoreservice.point.exception.invalid;

import com.nhnacademy._vidiabookstoreservice.global.exception.InvalidException;

public class InvalidGradeRateException extends InvalidException {
    public InvalidGradeRateException() {
        super("등급 적립률이 음수일 수 없습니다.");
    }
}
