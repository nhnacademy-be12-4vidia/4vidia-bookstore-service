package com.nhnacademy._vidiabookstoreservice.order.dto.order.request;

import java.util.List;

public record CouponCalculationRequest(
        Long couponId, // 선택된 쿠폰
        List<ItemInfo> items // 주문 도서 아이템
) {
    public record ItemInfo(
            // 쿠폰 서비스가 할인값의 결정권자가 되기 위해 책 정보 자체를 보내는게 좋음
            Long bookId,
            String categoryKdcId,
            int price,
            int quantity
    ) {}
}


