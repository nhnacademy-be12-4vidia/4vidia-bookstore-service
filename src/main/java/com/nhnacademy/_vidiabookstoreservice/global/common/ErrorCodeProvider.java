package com.nhnacademy._vidiabookstoreservice.global.common;

import org.springframework.http.HttpStatus;

public interface ErrorCodeProvider {
    HttpStatus getStatus();
    String getCode();
    String getMessage();
    String getName();
}
