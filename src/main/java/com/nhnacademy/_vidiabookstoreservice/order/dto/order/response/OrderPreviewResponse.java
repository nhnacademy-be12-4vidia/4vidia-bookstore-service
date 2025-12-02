package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderPreviewResponse(
        long orderId,
        long userId,
        LocalDateTime createdAt,
        DeliveryStatus deliveryStatus,
        List<OrderBookResponse> orderItems
) {
    public record OrderBookResponse(
            Long orderItemId,
            Long bookId,
            String bookTitle,
            String bookAuthor,
            String bookImageUrl,
            Integer quantity,
            Integer salePrice,
            ConfirmStatus confirmStatus
    ) {
        public static OrderBookResponse from(OrderItem orderItem) {
            return new OrderBookResponse(
                    orderItem.getOrderItemId(),
                    orderItem.getBook().getId(),
                    orderItem.getBook().getTitle(),
                    //orderItem.getBook().getBookAuthors().getFirst().getAuthor().getName(),
                    "작가이름",
                    //orderItem.getBook().getBookImageList().getFirst().getImageUrl(),
                    null,
                    orderItem.getQuantity(),
                    orderItem.getSalePrice(),
                    orderItem.getConfirmStatus()
            );
        }
    }

    public static OrderPreviewResponse from(Order order) {
        List<OrderBookResponse> orderItems = order.getOrderItems().stream()
                .map(OrderBookResponse::from)
                .toList();

        return new OrderPreviewResponse(
                order.getOrderId(),
                order.getUser().getUserId(),
                order.getCreatedAt(),
                order.getDeliveryStatus(),
                orderItems
        );
    }
}
