package com.nhnacademy._vidiabookstoreservice.refund.service;

import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundRequest;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundCountResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundHistoryGroupResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RefundService {
    RefundResponse getRefundList(long orderId);
    void refundRegister(RefundRequest refundRequest);
    Page<RefundHistoryGroupResponse> getMyRefunds(Long userId, RefundStatus status, Pageable pageable);
    RefundCountResponse getMyRefundCounts(Long userId);

}
