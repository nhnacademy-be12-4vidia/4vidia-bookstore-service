package com.nhnacademy._vidiabookstoreservice.order.dto.order.request;

public record OrderTrackingRequest(
        Long orderId,
        String orderPassword
) {
}
