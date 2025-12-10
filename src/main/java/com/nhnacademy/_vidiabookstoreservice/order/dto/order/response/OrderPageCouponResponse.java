package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;

import java.time.LocalDateTime;

public record OrderPageCouponResponse(
        Long couponId,
        String policyName,
        Integer maxDiscountAmount, // 최대할인금액
        String discountType,       // PRICE / RATE 구분
        Integer discountValue,     // 5000(₩) or 10(%)
        Integer discountPrice,     // 적용시 할인금액: 5000(₩) or 만원 보냈을 때 10% 적용 1000(₩)
        LocalDateTime expireAt,
        boolean available,
        String reason
) {}
