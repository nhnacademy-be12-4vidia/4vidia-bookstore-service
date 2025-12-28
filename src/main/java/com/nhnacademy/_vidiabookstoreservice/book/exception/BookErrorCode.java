package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.common.ErrorCodeProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum BookErrorCode implements ErrorCodeProvider {
    // NOT_FOUND (404)
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "도서를 찾을 수 없습니다."),
    AUTHOR_NOT_FOUND(HttpStatus.NOT_FOUND, "B002", "작가를 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "B003", "카테고리가 존재하지 않습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "B004", "리뷰를 찾을 수 없습니다."),
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "B005", "태그가 존재하지 않습니다."),
    DISCOUNT_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "B006", "할인 정책을 찾을 수 없습니다."),

    // BAD_REQUEST (400)

    // ALREADY_EXISTS (409)
    AUTHOR_ALREADY_EXISTS(HttpStatus.CONFLICT, "B201", "해당하는 이름의 작가가 이미 존재합니다."),
    BOOK_ALREADY_EXISTS(HttpStatus.CONFLICT, "B202", "이미 존재하는 도서입니다."),
    BOOK_AUTHOR_ALREADY_EXISTS(HttpStatus.CONFLICT, "B203", "이미 해당 도서에 등록된 작가입니다."),
    IMAGE_ALREADY_EXISTS(HttpStatus.CONFLICT, "B204", "해당 Url은 이미 저장되어있습니다."),
    BOOK_TAG_ALREADY_EXISTS(HttpStatus.CONFLICT, "B205", "이미 해당 도서에 등록된 태그입니다."),
    TAG_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "B206", "이미 존재하는 태그입니다"),
    CATEGORY_CANNOT_DELETE(HttpStatus.CONFLICT, "B207", "해당 카테고리를 삭제할 수 없습니다."),
    DISCOUNT_POLICY_ALREADY_EXISTS(HttpStatus.CONFLICT, "B208", "해당 카테고리(또는 전체)에 대한 할인 정책이 이미 존재합니다."),

    // UNPROCESSABLE_ENTITY(422) -> INVALID (요청 값 자체가 도메인 위반, 논리적 오류 : 나이 필드에 -1입력 등등)
    BOOK_AUTHOR_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "B301", "작가는 최소 한 명 이상 필요합니다."),

    // UNPROCESSABLE_ENTITY(422) -> NOT_ENOUGH (422)-> 입력 값/요청은 합리적, 단지 현재 상태가 부족
    BOOK_STOCK_NOT_ENOUGH(HttpStatus.UNPROCESSABLE_ENTITY, "B302", "현재 재고가 부족합니다."),
    BOOK_ISBN_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "B303", "유효하지 않은 ISBN 번호입니다."),

    // 500
    IMAGE_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "B401", "이미지 삭제 중 오류가 발생했습니다."),
    IMAGE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "B402", "이미지 업로드 중 오류가 발생했습니다."),

    // FORBIDDEN(403)
    REVIEW_USER_MISMATCH(HttpStatus.FORBIDDEN, "B501", "해당 상품을 주문한 사용자만 리뷰를 작성할 수 있습니다.")
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
