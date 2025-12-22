package com.nhnacademy._vidiabookstoreservice.admin.dto.refund;

import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;

public record RefundItemDto(
        Long refundItemId,
        String bookTitle,
        String bookImgUrl,
        int quantity,
        long salePrice
) {
    public static RefundItemDto of(RefundItem refundItem){
        return new RefundItemDto(
                refundItem.getRefundItemId(),
                refundItem.getOrderItem().getBook().getTitle(),
                // TODO 책 정보 가져오는거 수정
                refundItem.getOrderItem().getBook().getBookImageList().stream().findFirst().map(bookImage -> bookImage.getImageUrl()).orElse("null"),
                refundItem.getOrderItem().getQuantity(),
                refundItem.getOrderItem().getSalePrice()
        );
    }
}
