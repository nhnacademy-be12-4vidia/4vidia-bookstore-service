package com.nhnacademy._vidiabookstoreservice.refund.dto.response;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundItemStatus;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.exception.RefundItemNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record RefundHistoryGroupResponse(
        Long refundId,
        Long orderId,
        LocalDate orderDate,
        LocalDate refundDate,
        RefundStatus refundStatus,
        String refundReason, // 반품 사유
        Integer totalRefundPrice,

        List<RefundItemResponse> items
) {
    public record RefundItemResponse(
            String title,
            int quantity,
            int price,
            RefundItemStatus refundItemStatus,
            String rejectDetail // 거절 사유 (아이템별)

    ) {}

    public static RefundHistoryGroupResponse from(Refund refund) {
        List<RefundItem> refundItems = refund.getRefundItems();

        if (refundItems == null || refundItems.isEmpty()) {
            throw new RefundItemNotFoundException(refund.getRefundId());
        }

        List<RefundItemResponse> itemResponses =
                refundItems.stream()
                        .map(item -> new RefundItemResponse(
                                item.getOrderItem().getBook().getTitle(),
                                item.getOrderItem().getQuantity(),
                                item.getRefundPrice() == null ? 0 : item.getRefundPrice(),
                                item.getRefundItemStatus(),
                                item.getRejectDetail()
                        ))
                        .toList();

        Order order = refund.getOrder();

        int totalRefundPrice = refundItems.stream()
                .map(RefundItem::getRefundPrice)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();


        return new RefundHistoryGroupResponse(
                refund.getRefundId(),
                order.getOrderId(),
                order.getCreatedAt().toLocalDate(),
                refund.getCreatedAt().toLocalDate(),
                refund.getRefundStatus(),
                refund.getDescription(),
                totalRefundPrice,
                itemResponses
        );
    }
}
