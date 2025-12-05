package com.nhnacademy._vidiabookstoreservice.order.dto.order.request;

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

        int totalPrice, //도서 가격 합
        int deliveryCost, //배송비
        int packagingCost, //포장비

        int couponDiscount, //쿠폰할인금액
        int pointUsed,  //포인트사용금액
        int payPrice, //도서가격 + 배송비 + 포장비 - 할인/포인트

        List<ItemRequestDto> orderItems,
        //TODO 사용하는 쿠폰 아이디 리스트
        List<Long> coupons
) {
    public record ItemRequestDto(
            long bookId,
            int quantity,
            int salePrice,
            List<Long> packagingOptionIds
    ) {}
}// order.html에서 넘어오는 값
