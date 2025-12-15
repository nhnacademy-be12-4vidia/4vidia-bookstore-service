package com.nhnacademy._vidiabookstoreservice.point.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class PointCancelAlreadyExistsException extends AlreadyExistsException {
    public PointCancelAlreadyExistsException(Long orderId) {
        super("이미 포인트 환불이 진행되었습니다. orderId : " + orderId);
    }
}
