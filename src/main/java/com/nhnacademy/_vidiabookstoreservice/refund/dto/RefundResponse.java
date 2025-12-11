package com.nhnacademy._vidiabookstoreservice.refund.dto;

import java.util.List;

public record RefundResponse (
        Long orderId,
        List<OrderItemResponse> orderItems
){
}
