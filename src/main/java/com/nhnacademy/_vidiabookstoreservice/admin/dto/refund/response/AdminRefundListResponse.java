package com.nhnacademy._vidiabookstoreservice.admin.dto.refund.response;

import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.dto.RefundStatus;

import java.time.LocalDateTime;

public record AdminRefundListResponse(
        Long refundId,
        Long orderId,
        String email,
        String name,
        LocalDateTime createdAt,
        String refundStatus
) {
    public static AdminRefundListResponse from(Refund refund){
        return new AdminRefundListResponse(
                refund.getRefundId(),
                refund.getOrderItem().getOrder().getOrderId(),
                refund.getOrderItem().getOrder().getUser().getEmail(),
                refund.getOrderItem().getOrder().getUser().getName(),
                refund.getCreatedAt(),
                refund.getRefundStatus().name()
        );

    }
}
