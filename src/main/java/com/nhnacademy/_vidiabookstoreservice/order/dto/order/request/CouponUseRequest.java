package com.nhnacademy._vidiabookstoreservice.order.dto.order.request;

import java.util.List;

public record CouponUseRequest(
        Long orderId,
        List<Long> couponIds
) {
}
