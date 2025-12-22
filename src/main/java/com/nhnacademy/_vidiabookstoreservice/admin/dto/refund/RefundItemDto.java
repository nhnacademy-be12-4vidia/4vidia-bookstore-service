package com.nhnacademy._vidiabookstoreservice.admin.dto.refund;

import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;

public record RefundItemDto(
        Long refundItemId,
        String bookTitle,
        int quantity,
        long price
) {
    // TODO 수정 필요?
    public static RefundItemDto of(RefundItem refundItem){
        return new RefundItemDto(
                refundItem.getRefundItemId(),
                refundItem.getOrderItem().getBook().getTitle(),
                refundItem.getOrderItem().getQuantity(),
                refundItem.getOrderItem().getSalePrice()
        );
    }
}
