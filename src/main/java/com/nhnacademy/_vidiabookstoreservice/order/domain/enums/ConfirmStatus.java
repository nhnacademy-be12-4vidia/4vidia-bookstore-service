package com.nhnacademy._vidiabookstoreservice.order.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConfirmStatus {
    UNCONFIRMED(0),
    CONFIRMED(1),
    REFUND_REQUEST(2),
    REFUNDED(3),
    REFUND_REJECTED(4);

    private final int code;

    public static ConfirmStatus fromCode(int code) {
        for (ConfirmStatus status : ConfirmStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}
