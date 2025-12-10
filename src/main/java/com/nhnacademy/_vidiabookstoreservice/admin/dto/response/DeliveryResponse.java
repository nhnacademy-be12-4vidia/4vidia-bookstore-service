package com.nhnacademy._vidiabookstoreservice.admin.dto.response;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record DeliveryResponse(
        // list 화면에 출력할 정보
        long orderId,
        String email,
        String recipientName,
        String addressRoadname,
        String addressDetail,
        DeliveryStatus deliveryStatus,

        // 상세 추가
        int payPrice,
        String recipientPhone,
        String deliveryRequest,
        LocalDateTime createAt,
        int couponDiscount,
        int pointUsed,
        LocalDate deliveryDate,
        LocalDate actualDeliveryDate,
        List<OrderResponse.OrderBookResponse> orderItems
) {
    public static DeliveryResponse from(Order o) {
        String email = (o==null || o.getUser()==null) ? "비회원" : o.getUser().getEmail();

        return new DeliveryResponse(
                Objects.requireNonNull(o).getOrderId(),
                email,
                o.getRecipientName(),
                o.getAddressRoadname(),
                o.getAddressDetail(),
                o.getDeliveryStatus(),
                o.getPayPrice(),
                o.getRecipientPhone(),
                o.getDeliveryRequest(),
                o.getCreatedAt(),
                o.getCouponDiscount(),
                o.getPointUsed(),
                o.getDeliveryDate(),
                o.getActualDeliveryDate(),
                o.getOrderItems().stream().map(OrderResponse.OrderBookResponse::from).toList()
        );
    }

}
