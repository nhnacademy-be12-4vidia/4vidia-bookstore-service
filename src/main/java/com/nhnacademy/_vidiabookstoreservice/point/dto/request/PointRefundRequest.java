package com.nhnacademy._vidiabookstoreservice.point.dto.request;

public record PointRefundRequest(
        Long orderId,
        int amount
) {}
