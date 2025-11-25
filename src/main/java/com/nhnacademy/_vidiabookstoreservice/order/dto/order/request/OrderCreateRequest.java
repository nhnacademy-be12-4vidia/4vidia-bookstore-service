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
        int couponDiscount,
        int pointUsed,
        LocalDate deliveryDate,
        int totalPrice,
        int payPrice,
        List<OrderItem> orderItems
) {
}// order.html에서 넘어오는 값
