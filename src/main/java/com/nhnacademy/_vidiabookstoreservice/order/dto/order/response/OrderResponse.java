package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.packaging.response.PackagingResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record OrderResponse(
        long orderId,
        Long userId,
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
        int totalPrice, //도서 + 포장 + 배송
        int payPrice,   //도서 + 포장 + 배송 - 포인트 - 쿠폰
        List<OrderBookResponse> orderItems,
        int itemsPrice  //순수 도서 금액 (도서 * 수량)의 합
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
            List<PackagingResponse> packagingResponses,
            int totalPackagingPrice
    ) {
        public static OrderBookResponse from(OrderItem orderItem) {
            List<PackagingResponse> packagingOptions = (orderItem.getPackagings() != null) ?
                    orderItem.getPackagings().stream()
                            .map(PackagingResponse::from)
                            .toList() : Collections.emptyList();

            int packSum = packagingOptions.stream().mapToInt(PackagingResponse::price).sum();

            return new OrderBookResponse(
                    orderItem.getOrderItemId(),
                    orderItem.getBook().getId(),
                    orderItem.getBook().getTitle(),
                    orderItem.getBook().getBookAuthorList().stream().findFirst().map(author -> author.getAuthor().getName()).orElse("저자 미상"),
                    orderItem.getBook().getBookImageList().stream().findFirst().map(image -> image.getImageUrl()).orElse(null),
                    orderItem.getQuantity(),
                    orderItem.getSalePrice(),
                    orderItem.getConfirmStatus(),
                    packagingOptions,
                    packSum
            );
        }
    }

    public static OrderResponse from(Order order) {
        Long userId = null; // ⭐ 비회원이면 null
        if (order.getUser() != null) {
            userId = order.getUser().getUserId();
        }

        List<OrderBookResponse> orderItems = order.getOrderItems().stream()
                .map(OrderBookResponse::from)
                .toList();

        return new OrderResponse(
                order.getOrderId(),
                userId,
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
                orderItems,
                orderItems.stream().mapToInt(item -> item.salePrice() * item.quantity()).sum()
        );
    }
}
