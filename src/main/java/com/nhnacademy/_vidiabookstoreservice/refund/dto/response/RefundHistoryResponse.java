package com.nhnacademy._vidiabookstoreservice.refund.dto.response;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundStatus;

import java.time.LocalDate;

public record RefundHistoryResponse(
        Long refundId,
        Long orderId,
        LocalDate orderDate,
        String title,
        int price,
        int quantity,
        LocalDate returnDate,
        RefundStatus returnStatus,
        String returnReason
) {

    public static RefundHistoryResponse from(Refund refund) {
        OrderItem oi = refund.getOrderItem();
        Order order = oi.getOrder();

        return new RefundHistoryResponse(
                refund.getRefundId(),
                order.getOrderId(),
                order.getCreatedAt().toLocalDate(),
                oi.getBook().getTitle(),
                oi.getSalePrice(),
                oi.getQuantity(),
                refund.getCreatedAt().toLocalDate(),
                refund.getRefundStatus(),
                refund.getDescription()
        );
    }
}
