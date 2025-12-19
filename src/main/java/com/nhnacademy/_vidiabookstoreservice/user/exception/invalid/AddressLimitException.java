package com.nhnacademy._vidiabookstoreservice.user.exception.invalid;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserErrorCode;

public class AddressLimitException extends BaseException
{
    public AddressLimitException() {
        super(UserErrorCode.ADDRESS_LIMIT_INVALID);
    }
}
