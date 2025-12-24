package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;

public record OrderCountResponse(
    long total,
    long waiting,
    long shipping,
    long delivered,
    long canceled
) {}