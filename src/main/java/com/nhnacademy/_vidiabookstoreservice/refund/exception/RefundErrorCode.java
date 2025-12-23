package com.nhnacademy._vidiabookstoreservice.refund.exception;

import com.nhnacademy._vidiabookstoreservice.global.common.ErrorCodeProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum RefundErrorCode implements ErrorCodeProvider {
    // NOT_FOUND (404)
    REFUND_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "반품 내역을 찾을 수 없습니다."),
    REFUND_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "R002", "반품 아이템를 찾을 없습니다."),
    // BAD_REQUEST (400)

    // ALREADY_EXISTS (409)
    REFUND_ALREADY_APPROVED(HttpStatus.CONFLICT, "R201", "이미 승인된 반품입니다."),
    REFUND_ALREADY_REJECTED(HttpStatus.CONFLICT, "R202", "이미 거절된 반품입니다."),
    REFUND_ALREADY_PROGRESS(HttpStatus.CONFLICT, "R203", "반품이 진행 중이거나 완료된 상품은 구매 확정이 불가능합니다."),
    // UNPROCESSABLE_ENTITY(422) -> INVALID (요청 값 자체가 도메인 위반, 논리적 오류 : 나이 필드에 -1입력 등등)
    REFUND_PRICE_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "R301", "환불 금액은 음수일 수 없습니다."),
    REFUND_STATUS_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "R302", "반품 상태가 올바르지 않습니다."),

    // UNPROCESSABLE_ENTITY(422) -> NOT_ENOUGH (422)-> 입력 값/요청은 합리적, 단지 현재 상태가 부족
    REFUND_NOT_AVAILABLE(HttpStatus.UNPROCESSABLE_ENTITY, "R303", "단순 변심 반품은 배송 완료 후 10일 이내만 가능합니다.")
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
