package com.nhnacademy._vidiabookstoreservice.admin.dto.refund;

import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;

import java.time.LocalDateTime;
import java.util.List;

public record RefundDetailResponse(
        Long refundId,
        Long orderId,
        String email,
        String name,
        String description,
        LocalDateTime createdAt,
        RefundStatus refundStatus,
        List<RefundItemDto> items
) {
}
