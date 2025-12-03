package com.nhnacademy._vidiabookstoreservice.admin.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class PointPolicyNotFoundException extends NotFoundException {
    public PointPolicyNotFoundException(Long policyId) {
        super("포인트 정책 : %d을 찾을 수 없습니다.".formatted(policyId));
    }
}
