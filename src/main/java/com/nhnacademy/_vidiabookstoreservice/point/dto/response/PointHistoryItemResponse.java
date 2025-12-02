package com.nhnacademy._vidiabookstoreservice.point.dto.response;


import lombok.Builder;

import java.time.LocalDate;

@Builder
public record PointHistoryItemResponse(
        Long pointDetailId,
        String title,
        String orderNo,
        LocalDate createdDate,
        int amount,
        String displaySign
) {
}
