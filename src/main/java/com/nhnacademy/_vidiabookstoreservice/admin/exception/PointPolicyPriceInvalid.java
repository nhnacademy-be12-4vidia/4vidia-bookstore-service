package com.nhnacademy._vidiabookstoreservice.admin.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class PointPolicyPriceInvalid extends BaseException {
    public PointPolicyPriceInvalid() {
        super(AdminErrorCode.POINT_POLICY_PRICE_INVALID);
    }
}
