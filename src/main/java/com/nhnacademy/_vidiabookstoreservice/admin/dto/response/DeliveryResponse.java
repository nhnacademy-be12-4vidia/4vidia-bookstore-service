package com.nhnacademy._vidiabookstoreservice.admin.dto.response;

public record DeliveryResponse(
        Long orderId,
        String recipientName,
        String addressRoadname,
        String addressDetail,
        String recipientPhone,
        String deliveryStatus,
//        LocalDate deliveryDate,
        Integer payPrice,
//        LocalDateTime createdAt,
        String orderStatus
) {
}
