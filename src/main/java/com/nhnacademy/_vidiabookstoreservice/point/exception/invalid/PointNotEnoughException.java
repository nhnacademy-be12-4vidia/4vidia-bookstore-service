package com.nhnacademy._vidiabookstoreservice.point.exception.invalid;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.point.exception.PointErrorCode;

public class PointNotEnoughException extends BaseException {

    public PointNotEnoughException() {
        super(PointErrorCode.POINT_NOT_ENOUGH);
    }
}
