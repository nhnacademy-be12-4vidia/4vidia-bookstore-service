package com.nhnacademy._vidiabookstoreservice.admin.dto.refund;

import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;

public record RefundItemDto(
        Long refundItemId,
        RefundStatus refundStatus,
        String bookTitle,
        String bookImgUrl,
        int quantity,
        long salePrice
) {
    public static RefundItemDto of(RefundItem refundItem){
        return new RefundItemDto(
                refundItem.getRefundItemId(),
                refundItem.getRefundStatus(),
                refundItem.getOrderItem().getBook().getTitle(),
                // TODO 책 정보 가져오는거 수정
                refundItem.getOrderItem().getBook().getBookImageList().stream().findFirst().map(bookImage -> bookImage.getImageUrl()).orElse("null"),
                refundItem.getOrderItem().getQuantity(),
                refundItem.getOrderItem().getSalePrice()
        );
    }
}
