package com.nhnacademy._vidiabookstoreservice.point.domain;

public record PointRefundCommand(
        Long orderId,
        int refundPoint,
        int cashPoint
) {
}

