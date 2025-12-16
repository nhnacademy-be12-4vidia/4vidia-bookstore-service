package com.nhnacademy._vidiabookstoreservice.point.exception.invalid;

import com.nhnacademy._vidiabookstoreservice.global.exception.InvalidException;

public class GuestPointUseException extends InvalidException {
  public GuestPointUseException() {
    super("비회원은 포인트를 사용할 수 없습니다.");
  }
}
