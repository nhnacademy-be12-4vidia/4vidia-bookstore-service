package com.nhnacademy._vidiabookstoreservice.order.dto.order.request;

public record CouponUseRequest(
        Long orderId,
        Long couponId
) {
}
