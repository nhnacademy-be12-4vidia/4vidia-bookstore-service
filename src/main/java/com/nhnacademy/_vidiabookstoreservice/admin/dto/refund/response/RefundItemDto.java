package com.nhnacademy._vidiabookstoreservice.admin.dto.refund.response;

public record RefundItemDto(
        Long orderItemId,
        String bookTitle,
        int quantity,
        long price
) {}
