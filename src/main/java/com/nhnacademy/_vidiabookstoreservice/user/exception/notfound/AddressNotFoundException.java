package com.nhnacademy._vidiabookstoreservice.user.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserErrorCode;

public class AddressNotFoundException extends BaseException {
  public AddressNotFoundException(Long addressId) {
    super(UserErrorCode.ADDRESS_NOT_FOUND, "주소pk : " + addressId);
  }
}
