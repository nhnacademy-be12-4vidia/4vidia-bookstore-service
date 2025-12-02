package com.nhnacademy._vidiabookstoreservice.order.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class PackagingOptionNotFoundException extends NotFoundException {

    public PackagingOptionNotFoundException(Long packagingOptionId) {
        super("ID에 해당하는 포장을 찾을 수 없습니다. ID: %d".formatted(packagingOptionId));
    }
}
