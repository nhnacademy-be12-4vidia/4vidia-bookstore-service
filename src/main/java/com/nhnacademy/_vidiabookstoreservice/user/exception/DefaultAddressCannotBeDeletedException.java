package com.nhnacademy._vidiabookstoreservice.user.exception;

// TODO 상속 받는 exception 고민해보기
public class DefaultAddressCannotBeDeletedException extends RuntimeException {
  public DefaultAddressCannotBeDeletedException(String alias) {
    super("기본 주소는 삭제할 수 없습니다. 기본 주소를 변경 후 시도해주세요. 현재 기본 주소 별칭 : " + alias);
  }
}
