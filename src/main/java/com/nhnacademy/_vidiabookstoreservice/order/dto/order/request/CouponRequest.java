package com.nhnacademy._vidiabookstoreservice.order.dto.order.request;

import java.util.List;

public record CouponRequest(
        int amount,
        List<Long> bookIds,
        List<String> categoryKdcIds
) { }
