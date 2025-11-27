package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;

public record PackagingOptionResponse(
        long packagingOptionId,
        String name,
        int price
) {
}
