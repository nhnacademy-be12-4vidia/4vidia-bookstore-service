package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;

import java.util.List;

public record OrderCheckoutResponse(
        String orderName,
        int finalAmount,
        List<OrderBookResponse> bookItems,
        List<DeliveryDateResponse> deliveryDateResponses,
        List<PackagingOptionResponse> packagingOptions
) {
    public static OrderCheckoutResponse from (List<OrderBookResponse> bookItems,
                                              String orderName,
                                              int finalAmount,
                                              List<DeliveryDateResponse> deliveryDateResponses,
                                              List<PackagingOptionResponse> packagingOptions) {
        return new OrderCheckoutResponse(
                orderName,
                finalAmount,
                bookItems,
                deliveryDateResponses,
                packagingOptions
        );
    }
}
