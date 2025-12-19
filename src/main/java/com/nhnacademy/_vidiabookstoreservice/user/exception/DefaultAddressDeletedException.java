package com.nhnacademy._vidiabookstoreservice.user.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class DefaultAddressDeletedException extends BaseException {
  public DefaultAddressDeletedException(String alias) {
    super(UserErrorCode.DEFAULT_ADDRESS_DELETE, " 현재 기본 주소 별칭 : " + alias);
  }
}
