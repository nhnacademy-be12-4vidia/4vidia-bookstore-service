package com.nhnacademy._vidiabookstoreservice.point.dto.request;

public record PointUseRequest(
        Long orderId,
        int price
) {}
