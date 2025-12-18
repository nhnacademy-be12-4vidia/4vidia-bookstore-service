package com.nhnacademy._vidiabookstoreservice.refund.dto.request;

import jakarta.validation.constraints.NotNull;

public record RefundRejectRequest (
        @NotNull
        String rejectDetail
){
}
