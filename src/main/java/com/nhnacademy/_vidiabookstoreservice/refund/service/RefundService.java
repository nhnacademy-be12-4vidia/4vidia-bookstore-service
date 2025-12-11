package com.nhnacademy._vidiabookstoreservice.refund.service;

import com.nhnacademy._vidiabookstoreservice.refund.dto.RefundResponse;

public interface RefundService {
    RefundResponse getRefundList(long orderId);
}
