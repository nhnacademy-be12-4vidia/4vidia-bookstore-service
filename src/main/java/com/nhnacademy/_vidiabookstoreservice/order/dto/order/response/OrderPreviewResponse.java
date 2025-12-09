package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
            ConfirmStatus confirmStatus,
            Boolean isReviewed
    ) {
        public static OrderBookResponse from(OrderItem orderItem, Boolean isReviewed) {
            return new OrderBookResponse(
                    orderItem.getOrderItemId(),
                    orderItem.getBook().getId(),
                    orderItem.getBook().getTitle(),
                    orderItem.getBook().getBookAuthorList().stream().findFirst().map(author -> author.getAuthor().getName()).orElse("저자 미상"),
                    orderItem.getBook().getBookImageList().stream().findFirst().map(image -> image.getImageUrl()).orElse(null),
                    orderItem.getQuantity(),
                    orderItem.getSalePrice(),
                    orderItem.getConfirmStatus(),
                    isReviewed
            );
        }
    }

    public static OrderPreviewResponse from(Order order, Set<Long> reviewedItemIds) {
        List<OrderBookResponse> orderItems = order.getOrderItems().stream()
                .map(item ->  {
                    boolean isReviewed = reviewedItemIds.contains(item.getOrderItemId());
                    return OrderBookResponse.from(item, isReviewed);
                })
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
