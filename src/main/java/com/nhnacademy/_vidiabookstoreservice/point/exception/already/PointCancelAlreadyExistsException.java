package com.nhnacademy._vidiabookstoreservice.point.exception.already;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.point.exception.PointErrorCode;

public class PointCancelAlreadyExistsException extends BaseException {
    public PointCancelAlreadyExistsException(Long orderId) {
        super(PointErrorCode.POINT_CANCEL_ALREADY_EXISTS, "주문ID : %d".formatted(orderId));
    }
}
