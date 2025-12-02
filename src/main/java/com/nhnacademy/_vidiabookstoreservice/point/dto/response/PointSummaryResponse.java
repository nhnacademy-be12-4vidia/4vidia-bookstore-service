package com.nhnacademy._vidiabookstoreservice.point.dto.response;


import lombok.Builder;

@Builder
public record PointSummaryResponse(
        int totalPoint,
        int nextMonthExpirePoint
) {
}
