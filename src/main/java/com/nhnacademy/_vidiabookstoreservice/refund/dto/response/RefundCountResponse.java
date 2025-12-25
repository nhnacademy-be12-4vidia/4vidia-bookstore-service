package com.nhnacademy._vidiabookstoreservice.refund.dto.response;

public record RefundCountResponse(
        long total,
        long process,
        long approved
) {}
