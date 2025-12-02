package com.nhnacademy._vidiabookstoreservice.user.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class AddressNotFoundException extends NotFoundException {
  public AddressNotFoundException(Long addressId) {
    super("해당 회원의 주소를 찾을 수 없습니다. 주소pk : " + addressId);
  }
}
