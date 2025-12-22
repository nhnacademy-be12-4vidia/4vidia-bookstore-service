package com.nhnacademy._vidiabookstoreservice.refund.dto.request;

import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import jakarta.validation.constraints.NotNull;

public record RefundItemUpdateRequest (
        @NotNull
        int refundPrice,
        @NotNull
        RefundStatus refundStatus,
        String rejectDetail // 거절에만
) {
}
