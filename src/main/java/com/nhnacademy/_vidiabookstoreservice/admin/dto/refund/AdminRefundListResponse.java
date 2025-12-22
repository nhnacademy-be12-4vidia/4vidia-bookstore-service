package com.nhnacademy._vidiabookstoreservice.admin.dto.refund;

import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;

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
                refund.getOrder().getOrderId(),
                refund.getOrder().getUser().getEmail(),
                refund.getOrder().getUser().getName(),
                refund.getCreatedAt(),
                refund.getRefundStatus().name()
        );

    }
}
