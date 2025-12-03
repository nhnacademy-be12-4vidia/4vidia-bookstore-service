package com.nhnacademy._vidiabookstoreservice.order.dto.order.request;

public record OrderCheckoutRequest(
        Long bookId,
        int quantity
) { }
