package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;

public record BookOrderResponse(
        Long id,
        String title,
        String author,
        String imageUrl,
        Integer salePrice
) { }
