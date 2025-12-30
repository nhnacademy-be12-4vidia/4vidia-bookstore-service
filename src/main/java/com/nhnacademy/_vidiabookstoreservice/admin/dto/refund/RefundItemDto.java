package com.nhnacademy._vidiabookstoreservice.admin.dto.refund;

import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundItemStatus;

public record RefundItemDto(
        Long refundItemId,
        RefundItemStatus refundItemStatus,
        String bookTitle,
        String bookImgUrl,
        int quantity,
        long salePrice
) {
    public static RefundItemDto of(RefundItem refundItem){
        return new RefundItemDto(
                refundItem.getRefundItemId(),
                refundItem.getRefundItemStatus(),
                refundItem.getOrderItem().getBook().getTitle(),
                refundItem.getOrderItem().getBook().getBookImageList().stream().findFirst().map(BookImage::getImageUrl).orElse("null"),
                refundItem.getOrderItem().getQuantity(),
                refundItem.getOrderItem().getSalePrice()
        );
    }
}
