package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItemViewStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItemViewStatusResolver;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            OrderItemViewStatus orderItemViewStatus,
            Boolean isReviewed
    ) {
        public static OrderBookResponse from(OrderItem orderItem, Boolean isReviewed, OrderItemViewStatusResolver resolver, Optional<RefundItem> refundItem) {
            return new OrderBookResponse(
                    orderItem.getOrderItemId(),
                    orderItem.getBook().getId(),
                    orderItem.getBook().getTitle(),
                    orderItem.getBook().getBookAuthorList().stream().findFirst().map(author -> author.getAuthor().getName()).orElse("저자 미상"),
                    orderItem.getBook().getBookImageList().stream().findFirst().map(image -> image.getImageUrl()).orElse(null),
                    orderItem.getQuantity(),
                    orderItem.getSalePrice(),
                    resolver.resolve(orderItem, refundItem),
                    isReviewed
            );
        }
    }

    public static OrderPreviewResponse from(Order order, Set<Long> reviewedItemIds, Set<RefundItem> refundItems, OrderItemViewStatusResolver resolver) {
        List<OrderBookResponse> orderItems = order.getOrderItems().stream()
                .map(item ->  {
                    boolean isReviewed = reviewedItemIds.contains(item.getOrderItemId());
                    Optional<RefundItem> refundItem = refundItems.stream()
                            .filter(r -> r.getOrderItem().getOrderItemId().equals(item.getOrderItemId()))
                            .findFirst();
                    return OrderBookResponse.from(item, isReviewed, resolver, refundItem);
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
