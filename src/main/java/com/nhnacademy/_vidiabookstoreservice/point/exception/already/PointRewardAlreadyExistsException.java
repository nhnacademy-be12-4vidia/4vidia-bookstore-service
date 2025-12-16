package com.nhnacademy._vidiabookstoreservice.point.exception.already;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;

public class PointRewardAlreadyExistsException extends AlreadyExistsException {
    public PointRewardAlreadyExistsException(Long orderId) {
        super("해당 주문에 대한 적립이 이미 존재합니다. 주문번호 : %d".formatted(orderId));
    }
}
