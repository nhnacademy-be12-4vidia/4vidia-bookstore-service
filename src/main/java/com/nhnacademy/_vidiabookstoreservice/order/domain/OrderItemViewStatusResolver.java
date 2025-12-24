package com.nhnacademy._vidiabookstoreservice.order.domain;

import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderItemViewStatusResolver {

    public OrderItemViewStatus resolve(
            OrderItem item,
            Optional<RefundItem> refundItem
    ) {
        // 우선순위 : 구매 확정 > 반품 거절
        if (item.confirmStatus == ConfirmStatus.CONFIRMED) {
            return OrderItemViewStatus.CONFIRMED;
        }

        return refundItem.map(value -> switch (value.getRefundItemStatus()) {
            case PROCESS -> OrderItemViewStatus.REFUND_REQUESTED;
            case APPROVED -> OrderItemViewStatus.REFUNDED;
            case REJECTED -> OrderItemViewStatus.REFUND_REJECTED;
        }).orElse(OrderItemViewStatus.UNCONFIRMED);

    }
}