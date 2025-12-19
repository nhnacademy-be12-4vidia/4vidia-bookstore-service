package com.nhnacademy._vidiabookstoreservice.user.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserErrorCode;

public class DefaultAddressNotFoundException extends BaseException {
    public DefaultAddressNotFoundException() {
        super(UserErrorCode.DEFAULT_ADDRESS_NOT_FOUND);
    }
}
