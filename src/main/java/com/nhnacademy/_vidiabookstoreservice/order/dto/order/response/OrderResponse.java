package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;

import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrderResponse(
        long orderId,
        long userId,
        String recipientName,
        String addressRoadname,
        String addressDetail,
        String zipCode,
        String recipientPhone,
        String deliveryRequest,
        LocalDateTime createdAt,
        int couponDiscount,
        int pointUsed,
        LocalDate deliveryDate,
        DeliveryStatus deliveryStatus,
        LocalDate actualDeliveryDate, //null값 가져올수도있음
        int totalPrice,
        int payPrice
) {
}
