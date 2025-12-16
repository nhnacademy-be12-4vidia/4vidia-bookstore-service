package com.nhnacademy._vidiabookstoreservice.point.exception.notenough;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotEnoughException;

public class UnexpirePointUseException extends NotEnoughException {
    public UnexpirePointUseException() {
        super("만료된 포인트가 포함되어 실제 사용 가능한 포인트가 부족합니다.");
    }
}
