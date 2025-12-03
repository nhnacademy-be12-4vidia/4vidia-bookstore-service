package com.nhnacademy._vidiabookstoreservice.cart.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class PointPolicyNotFoundException extends NotFoundException {
    public PointPolicyNotFoundException(Long policyId) {
        super("포인트정책 아이디:%d 정책을 찾을 수 없음".formatted(policyId));
    }
}
