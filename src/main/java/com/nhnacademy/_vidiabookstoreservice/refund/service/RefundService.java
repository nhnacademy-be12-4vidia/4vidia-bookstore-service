package com.nhnacademy._vidiabookstoreservice.refund.service;

import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundRequest;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundResponse;

public interface RefundService {
    RefundResponse getRefundList(long orderId);
    void refundRegister(RefundRequest refundRequest);
}
