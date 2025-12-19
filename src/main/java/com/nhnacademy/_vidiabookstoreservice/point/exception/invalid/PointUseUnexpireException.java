package com.nhnacademy._vidiabookstoreservice.point.exception.invalid;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.point.exception.PointErrorCode;

public class PointUseUnexpireException extends BaseException {
    public PointUseUnexpireException() {
        super(PointErrorCode.POINT_USE_UNEXPIRE);
    }
}
