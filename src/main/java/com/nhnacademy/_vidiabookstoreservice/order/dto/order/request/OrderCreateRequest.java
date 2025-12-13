package com.nhnacademy._vidiabookstoreservice.order.dto.order.request;

import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.util.List;

public record OrderCreateRequest(
        String recipientName,
        String addressRoadname,
        String addressDetail,
        String zipCode,
        String recipientPhone,
        String deliveryRequest,
        LocalDate deliveryDate,
        String orderPassword,

        @PositiveOrZero(message = "도서 가격은 0 이상이어야 합니다.")
        int totalPrice, //도서 가격 합
        @PositiveOrZero(message = "배송비는 0 이상이어야 합니다.")
        int deliveryCost, //배송비
        @PositiveOrZero(message = "포장비는 0 이상이어야 합니다.")
        int packagingCost, //포장비

        int couponDiscount, //쿠폰할인금액
        @PositiveOrZero(message = "사용 포인트는 0 이상이어야 합니다.")
        int pointUsed,  //포인트사용금액
        @PositiveOrZero(message = "최소 결제금액은 0 이상이어야 합니다.")
        int payPrice, //도서가격 + 배송비 + 포장비 - 할인/포인트

        List<ItemRequestDto> orderItems,
        Long couponId
) {
    public record ItemRequestDto(
            long bookId,
            int quantity,
            int salePrice,
            List<Long> packagingOptionIds
    ) {}
}// order.html에서 넘어오는 값
