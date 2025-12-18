package com.nhnacademy._vidiabookstoreservice.refund.dto.response;

public record UseCouponResponse (
    String discountTargetType, //all, category, book
    String categoryKdcId,
    Long bookId,
    Integer minOrderAmount // 최소 주문 금액
) {
}
