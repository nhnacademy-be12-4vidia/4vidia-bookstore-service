package com.nhnacademy._vidiabookstoreservice.point.exception.notenough;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotEnoughException;

public class NotEnoughPointException extends NotEnoughException {

    public NotEnoughPointException() {
        super("현재 보유 포인트가 부족합니다");
    }
}
