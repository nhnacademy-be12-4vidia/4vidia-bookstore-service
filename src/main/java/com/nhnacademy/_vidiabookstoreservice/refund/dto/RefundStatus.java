package com.nhnacademy._vidiabookstoreservice.refund.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RefundStatus {
    PROCESS(0),
    ACCEPT(1),
    REJECT(2);

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
