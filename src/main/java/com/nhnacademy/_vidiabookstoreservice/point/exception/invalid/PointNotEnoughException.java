package com.nhnacademy._vidiabookstoreservice.point.exception.invalid;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotEnoughException;

public class PointNotEnoughException extends NotEnoughException {

    public PointNotEnoughException() {
        super("현재 보유 포인트가 부족합니다");
    }
}
