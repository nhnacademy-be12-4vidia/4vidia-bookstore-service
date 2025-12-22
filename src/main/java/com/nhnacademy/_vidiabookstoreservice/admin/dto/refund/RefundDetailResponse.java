package com.nhnacademy._vidiabookstoreservice.admin.dto.refund;

import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;

import java.time.LocalDateTime;
import java.util.List;

public record RefundDetailResponse(
        Long refundId,
        Long orderId,
        String email,
        String name,
        String description,
        LocalDateTime createdAt,
        List<RefundItemDto> items
) {
}
