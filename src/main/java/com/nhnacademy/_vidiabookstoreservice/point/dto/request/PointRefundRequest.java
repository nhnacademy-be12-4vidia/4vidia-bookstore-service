package com.nhnacademy._vidiabookstoreservice.point.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PointRefundRequest(
        @NotNull
        Long orderId,
        @NotNull
        @Min(0)
        int refundPoint,
        @NotNull
        @Min(0)
        int cashPoint
) {
}

