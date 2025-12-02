package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
        int payPrice,
        List<OrderItemResponse> orderItems
) {
    public record OrderItemResponse(
            Long orderItemId,
            Long bookId,
            Integer quantity,
            Integer salePrice,
            ConfirmStatus confirmStatus
    ) {
        public static OrderItemResponse from(OrderItem orderItem) {
            return new OrderItemResponse(
                    orderItem.getOrderItemId(),
                    orderItem.getBook().getId(),
                    orderItem.getQuantity(),
                    orderItem.getSalePrice(),
                    orderItem.getConfirmStatus()
            );
        }
    }

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> orderItems = order.getOrderItems().stream()
                .map(OrderItemResponse::from)
                .toList();

        return new OrderResponse(
                order.getOrderId(),
                order.getUser().getUserId(),
                order.getRecipientName(),
                order.getAddressRoadname(),
                order.getAddressDetail(),
                order.getZipCode(),
                order.getRecipientPhone(),
                order.getDeliveryRequest(),
                order.getCreatedAt(),
                order.getCouponDiscount(),
                order.getPointUsed(),
                order.getDeliveryDate(),
                order.getDeliveryStatus(),
                order.getActualDeliveryDate(),
                order.getTotalPrice(),
                order.getPayPrice(),
                orderItems
        );
    }
}
