package com.nhnacademy._vidiabookstoreservice.order.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class PackagingNotFoundException extends NotFoundException {

    public PackagingNotFoundException(String message) {
        super(message);
    }
}
