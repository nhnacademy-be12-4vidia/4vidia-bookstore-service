package com.nhnacademy._vidiabookstoreservice.refund.dto.request;

import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundItemStatus;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import jakarta.validation.constraints.NotNull;

public record RefundItemUpdateRequest (
        @NotNull
        RefundItemStatus refundItemStatus,
        String rejectDetail // 거절에만 작성
) {
}
