package com.nhnacademy._vidiabookstoreservice.order.exception;

import com.nhnacademy._vidiabookstoreservice.global.common.ErrorCodeProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum OrderErrorCode implements ErrorCodeProvider {
    // NOT_FOUND (404)
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문아이템을 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O002", "주문 내역을 찾을 수 없습니다."),
    PACKAGING_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "O003", "포장을 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "O004", "결제내역을 찾을 수 없습니다."),
    // BAD_REQUEST (400)

    // ALREADY_EXISTS (409)
    ORDER_AMOUNT_MISMATCH(HttpStatus.CONFLICT, "O201", "주문 금액에 변화가 있습니다."),
    // UNPROCESSABLE_ENTITY(422) -> INVALID (요청 값 자체가 도메인 위반, 논리적 오류 : 나이 필드에 -1입력 등등)

    // UNPROCESSABLE_ENTITY(422) -> NOT_ENOUGH (422)-> 입력 값/요청은 합리적, 단지 현재 상태가 부족

    // 500
    ORDER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "O501", "주문 처리 중 오류가 발생했습니다."),
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
