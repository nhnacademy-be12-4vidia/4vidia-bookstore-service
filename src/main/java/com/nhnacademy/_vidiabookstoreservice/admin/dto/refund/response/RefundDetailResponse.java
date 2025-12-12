package com.nhnacademy._vidiabookstoreservice.admin.dto.refund.response;

import java.time.LocalDateTime;
import java.util.List;

public record RefundDetailResponse(
        Long refundId,
        Long orderId,
        String email,
        String name,
        String description,
        LocalDateTime createdAt,
        String refundStatus,
        List<RefundItemDto> items
) {}
