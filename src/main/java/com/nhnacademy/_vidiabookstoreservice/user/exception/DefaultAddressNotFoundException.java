package com.nhnacademy._vidiabookstoreservice.user.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class DefaultAddressNotFoundException extends NotFoundException {
    public DefaultAddressNotFoundException() {
        super("기본 주소가 설정되어 있지 않습니다.");
    }
}
