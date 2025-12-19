package com.nhnacademy._vidiabookstoreservice.point.exception.invalid;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.point.exception.PointErrorCode;

public class PointGuestUseException extends BaseException {
  public PointGuestUseException() {
    super(PointErrorCode.POINT_GUEST_USE);
  }
}
