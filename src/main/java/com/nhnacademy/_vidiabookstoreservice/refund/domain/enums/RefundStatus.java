package com.nhnacademy._vidiabookstoreservice.refund.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RefundStatus {
    PROCESS(0), // 반품 신청 ~ 결과 나오기 전까지
    APPROVED(1), // 승인
    REJECTED(2); // 거절

    private final int code;

    public static RefundStatus fromCode(int code) {
        for (RefundStatus status : RefundStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}
