package com.nhnacademy._vidiabookstoreservice.order.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderErrorCode;

public class PackagingOptionNotFoundException extends BaseException {

    public PackagingOptionNotFoundException(Long packagingOptionId) {
        super(OrderErrorCode.PACKAGING_OPTION_NOT_FOUND, "포장 ID: %d".formatted(packagingOptionId));
    }
}
