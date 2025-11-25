package com.nhnacademy._vidiabookstoreservice.order.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeliveryStatus {
    WAITING(0),
    SHIPPING(1),
    DELIVERED(2),
    CANCELED(3);

    private final int code;

    public static DeliveryStatus fromCode(int code) {
        for (DeliveryStatus status : DeliveryStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}
