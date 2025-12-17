package com.nhnacademy._vidiabookstoreservice.user.exception;

public class MaxAddressLimitExceededException extends RuntimeException
{
    public MaxAddressLimitExceededException(int max) {
        super("배송지는 최대 %d까지만 등록할 수 있습니다.".formatted(max));
    }
}
