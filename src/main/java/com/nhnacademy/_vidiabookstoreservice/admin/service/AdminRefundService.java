package com.nhnacademy._vidiabookstoreservice.admin.service;

import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.AdminRefundListResponse;
import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.RefundDetailResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminRefundService {
    Page<AdminRefundListResponse> listByRefundStatus(RefundStatus refundStatus, String keyword, Pageable pageable);
    RefundDetailResponse getRefundDetail(Long refundId);
    void acceptRefund(Long refundId);
    void rejectRefund(Long refundId);
}
