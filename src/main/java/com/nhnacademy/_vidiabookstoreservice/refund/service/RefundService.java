package com.nhnacademy._vidiabookstoreservice.refund.service;

import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundRequest;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundHistoryGroupResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundResponse;

import java.util.List;

public interface RefundService {
    RefundResponse getRefundList(long orderId);
    void refundRegister(RefundRequest refundRequest);
    List<RefundHistoryGroupResponse> getMyRefunds(Long userId, RefundStatus status);
}
