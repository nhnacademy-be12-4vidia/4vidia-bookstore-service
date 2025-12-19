package com.nhnacademy._vidiabookstoreservice.cart.exception;

import com.nhnacademy._vidiabookstoreservice.global.common.ErrorCodeProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum CartErrorCode implements ErrorCodeProvider {
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "장바구니를 찾을 수 없습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
