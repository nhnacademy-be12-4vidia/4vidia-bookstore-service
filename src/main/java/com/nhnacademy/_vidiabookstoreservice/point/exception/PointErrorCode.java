package com.nhnacademy._vidiabookstoreservice.point.exception;

import com.nhnacademy._vidiabookstoreservice.global.common.ErrorCodeProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum PointErrorCode implements ErrorCodeProvider {
    // NOT_FOUND (404)
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "해당 주문에 사용된 포인트 내역을 찾을 수 없습니다."),

    // BAD_REQUEST (400)

    // ALREADY_EXISTS (409)
    POINT_CANCEL_ALREADY_EXISTS(HttpStatus.CONFLICT, "P201", "이미 포인트 환불이 진행되었습니다."),
    POINT_REWARD_ALREADY_EXISTS(HttpStatus.CONFLICT, "P202", "해당 주문에 대한 적립이 이미 존재합니다."),

    // UNPROCESSABLE_ENTITY(422) -> INVALID (요청 값 자체가 도메인 위반, 논리적 오류 : 나이 필드에 -1입력 등등)
    POINT_GUEST_USE(HttpStatus.UNPROCESSABLE_ENTITY, "P301", "비회원은 포인트를 사용할 수 없습니다."),
    POINT_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "P302", "사용/적립하는 포인트 값은 음수일 수 없습니다."),

    // UNPROCESSABLE_ENTITY(422) -> NOT_ENOUGH (422)-> 입력 값/요청은 합리적, 단지 현재 상태가 부족
    POINT_NOT_ENOUGH(HttpStatus.UNPROCESSABLE_ENTITY, "P303", "현재 보유 포인트가 부족합니다"),
    POINT_USE_UNEXPIRE(HttpStatus.UNPROCESSABLE_ENTITY, "P304", "만료된 포인트가 포함되어 실제 사용 가능한 포인트가 부족합니다.");

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
    public String getName(){
        return this.name();
    }
}
