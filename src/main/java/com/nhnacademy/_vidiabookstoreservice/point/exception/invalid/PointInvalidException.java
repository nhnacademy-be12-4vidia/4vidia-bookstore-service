package com.nhnacademy._vidiabookstoreservice.point.exception.invalid;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.point.exception.PointErrorCode;

public class PointInvalidException extends BaseException {
    public PointInvalidException() {
        super(PointErrorCode.POINT_INVALID);
    }
}
