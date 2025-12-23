package com.nhnacademy._vidiabookstoreservice.user.exception;

import com.nhnacademy._vidiabookstoreservice.global.common.ErrorCodeProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum UserErrorCode implements ErrorCodeProvider {
    // NOT_FOUND (404)
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "주소를 찾을 수 없습니다."),
    DEFAULT_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "기본 주소가 설정되어 있지 않습니다."),
    GRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "U003", "존재하지 않는 등급입니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "U004", "좋아요를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U005", "일치하는 회원정보가 없습니다."),
    // BAD_REQUEST (400)

    // ALREADY_EXISTS (409)
    LIKED_ALREADY_EXISTS(HttpStatus.CONFLICT, "U201", "이미 좋아요 처리가 되어있습니다."),
    RESIGNED_USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U202", "이미 탈퇴한 회원입니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U203", "이미 존재하는 회원입니다."),
    DEFAULT_ADDRESS_DELETE(HttpStatus.CONFLICT, "U204", "기본 주소는 삭제할 수 없습니다. 기본 주소를 변경 후 시도해주세요."),

    // UNPROCESSABLE_ENTITY(422) -> INVALID (요청 값 자체가 도메인 위반, 논리적 오류 : 나이 필드에 -1입력 등등)
    GRADE_RATE_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "U301", "등급 적립률이 음수일 수 없습니다."),
    ADDRESS_LIMIT_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "U302", "배송지는 최대 10개까지만 등록할 수 있습니다."),
    PASSWORD_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "U303", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."),
    SAME_PASSWORD(HttpStatus.UNPROCESSABLE_ENTITY, "U304", "현재 비밀번호와 동일한 비밀번호로는 변경할 수 없습니다."),
    // UNPROCESSABLE_ENTITY(422) -> NOT_ENOUGH (422)-> 입력 값/요청은 합리적, 단지 현재 상태가 부족

    // Unauthorized (401)
    AUTH_CODE_EXPIRED(HttpStatus.UNAUTHORIZED, "U501", "인증 코드가 만료되었습니다."),
    INCORRECT_PASSWORD(HttpStatus.UNAUTHORIZED, "U502", "비밀번호가 일치하지 않습니다."),
    INVALID_AUTH_CODE(HttpStatus.UNAUTHORIZED, "U503", "인증 코드가 일치하지 않습니다."),

    // SERVICE_UNAVAILABLE (503)
    EMAIL_SEND_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "U601", "이메일 전송에 실패했습니다.");

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
