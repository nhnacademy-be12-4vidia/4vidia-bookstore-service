package com.nhnacademy._vidiabookstoreservice.refund.dto.request;

import java.util.List;

public record RefundRequest (
        Long orderId,
        String reason,
        boolean damaged,
        List<Long> orderItemIds
){
}