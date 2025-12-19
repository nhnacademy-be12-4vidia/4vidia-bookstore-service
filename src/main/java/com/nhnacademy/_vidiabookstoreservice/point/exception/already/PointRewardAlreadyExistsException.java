package com.nhnacademy._vidiabookstoreservice.point.exception.already;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.point.exception.PointErrorCode;

public class PointRewardAlreadyExistsException extends BaseException {
    public PointRewardAlreadyExistsException(Long orderId) {
        super(PointErrorCode.POINT_REWARD_ALREADY_EXISTS, "주문번호 : %d".formatted(orderId));
    }
}
