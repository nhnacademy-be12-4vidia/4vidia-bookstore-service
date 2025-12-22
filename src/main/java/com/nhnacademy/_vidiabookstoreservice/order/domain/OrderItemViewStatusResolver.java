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
        if (refundItem.isPresent()) {
            switch (refundItem.get().getRefundStatus()) {
                case PROCESS:
                    return OrderItemViewStatus.REFUND_REQUESTED;
                case APPROVED:
                    return OrderItemViewStatus.REFUNDED;
                case REJECTED:
                    return OrderItemViewStatus.REFUND_REJECTED;
            }
        }

        if (item.confirmStatus == ConfirmStatus.CONFIRMED) {
            return OrderItemViewStatus.CONFIRMED;
        }

        return OrderItemViewStatus.UNCONFIRMED;
    }
}