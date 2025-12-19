package com.nhnacademy._vidiabookstoreservice.admin.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;

public class DeliveryEndInvalidException extends BaseException {
    public DeliveryEndInvalidException(DeliveryStatus status) {
        super(AdminErrorCode.DELIVERY_END_INVALID, " 현재 상태: %s".formatted(status));
    }
}
