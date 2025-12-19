package com.nhnacademy._vidiabookstoreservice.global.exception;

import com.nhnacademy._vidiabookstoreservice.global.common.ErrorCodeProvider;
import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
    private final ErrorCodeProvider errorCode;
    private final String formattedMessage;

    protected BaseException(ErrorCodeProvider errorCode, Object... args){
        super(String.format(errorCode.getMessage(), args));
        this.errorCode = errorCode;
        this.formattedMessage = String.format(errorCode.getMessage(), args);
    }
}
