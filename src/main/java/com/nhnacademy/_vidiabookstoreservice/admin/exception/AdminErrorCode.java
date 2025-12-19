package com.nhnacademy._vidiabookstoreservice.admin.exception;

import com.nhnacademy._vidiabookstoreservice.global.common.ErrorCodeProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum AdminErrorCode implements ErrorCodeProvider {
    POINT_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "A001", "포인트 정책을 찾을 수 없습니다."),

    DELIVERY_START_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "A301", "배송 시작은 WAITING 상태에서만 가능합니다."),
    DELIVERY_END_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "A302", "배송 완료는 SHIPPING 상태에서만 가능합니다."),
    POINT_POLICY_PRICE_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "A303", "포인트 정책 : price는 null이거나 음수일 수 없습니다.");

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
