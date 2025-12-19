package com.nhnacademy._vidiabookstoreservice.admin.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class PointPolicyNotFoundException extends BaseException {
    public PointPolicyNotFoundException(Long policyId) {
        super(AdminErrorCode.POINT_POLICY_NOT_FOUND, "정책 아이디: %d".formatted(policyId));
    }
}
