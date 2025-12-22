package com.nhnacademy._vidiabookstoreservice.refund.dto.response;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;

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

        // 🔒 방어 코드 (매우 중요)
        if (refund.getRefundItems().isEmpty()) {
            throw new IllegalStateException(
                    "RefundItem not exists. refundId=" + refund.getRefundId()
            );
        }

        // ✅ RefundItem 기준으로 조회
        RefundItem refundItem = refund.getRefundItems().get(0);
        OrderItem oi = refundItem.getOrderItem();
        Order order = refund.getOrder();

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
