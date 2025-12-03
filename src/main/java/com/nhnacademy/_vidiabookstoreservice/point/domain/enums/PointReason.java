package com.nhnacademy._vidiabookstoreservice.point.domain.enums;


import lombok.Getter;
@Getter
public enum PointReason {
    ORDER_REWARD(0, "상품 구매 적립"),
    POLICY_REWARD(1, "포인트 정책 적립"),
    ORDER_USE(2, "포인트 사용"),
    ORDER_CANCEL_REFUND(3, "주문 취소 환불 포인트"),
    POINT_EXPIRE(4,"기간 만료로 인한 소멸");

    private final int code;
    private final String title;

    PointReason(int code, String title) {
        this.code = code;
        this.title = title;
    }

    public static PointReason of(int code) {
        for (PointReason reason : values()) {
            if (reason.code == code) return reason;
        }
        throw new IllegalArgumentException("Invalid PointReason code: " + code);
    }
}