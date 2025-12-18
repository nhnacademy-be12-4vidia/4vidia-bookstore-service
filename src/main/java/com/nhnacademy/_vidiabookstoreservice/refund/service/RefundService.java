package com.nhnacademy._vidiabookstoreservice.refund.service;

import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundRequest;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundHistoryResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundResponse;

import java.util.List;

public interface RefundService {
    RefundResponse getRefundList(long orderId);
    void refundRegister(RefundRequest refundRequest);
    List<RefundHistoryResponse> getMyRefunds(Long userId, RefundStatus status);
}
