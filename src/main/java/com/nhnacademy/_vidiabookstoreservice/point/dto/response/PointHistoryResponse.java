package com.nhnacademy._vidiabookstoreservice.point.dto.response;

import java.time.LocalDateTime;

public record PointHistoryResponse(
        LocalDateTime createdAt,
        Integer price,
        String reason,
        Long orderId,
        LocalDateTime expiredAt
) {
}
