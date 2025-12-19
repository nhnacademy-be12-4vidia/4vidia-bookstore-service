package com.nhnacademy._vidiabookstoreservice.point.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.point.exception.PointErrorCode;

public class PointNotFoundException extends BaseException {
    public PointNotFoundException(Long orderId) {
        super(PointErrorCode.POINT_NOT_FOUND, "주문ID: %d".formatted(orderId));
    }
}
