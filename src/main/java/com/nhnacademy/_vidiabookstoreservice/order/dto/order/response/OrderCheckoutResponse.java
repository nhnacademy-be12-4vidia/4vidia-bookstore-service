package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;

import com.nhnacademy._vidiabookstoreservice.user.dto.address.response.AddressResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.OrderUserResponse;

import java.util.List;

public record OrderCheckoutResponse(
        List<OrderBookResponse> bookItems,
        String orderName,
        int finalAmount,

        String name,
        String email,
        String phone,
        Integer point,
        List<AddressResponse> addressResponses,

        List<DeliveryDateResponse> deliveryDateResponses,
        List<OrderPageCouponResponse> possibleCoupons,
        List<OrderPageCouponResponse> impossibleCoupons,
        List<PackagingOptionResponse> packagingOptions
) {
    public static OrderCheckoutResponse from(OrderUserResponse orderUserResponse,
                                         List<OrderBookResponse> bookItems,
                                         String orderName,
                                         int finalAmount,
                                         List<OrderPageCouponResponse> possibleCoupons,
                                         List<OrderPageCouponResponse> impossibleCoupons,
                                         List<DeliveryDateResponse> deliveryDateResponses,
                                         List<PackagingOptionResponse> packagingOptions) {
        return new OrderCheckoutResponse(
                bookItems,
                orderName,
                finalAmount,
                orderUserResponse.name(),
                orderUserResponse.email(),
                orderUserResponse.phone(),
                orderUserResponse.point(),
                orderUserResponse.addressResponses(),
                deliveryDateResponses,
//                possibleCoupons,
//                impossibleCoupons,
                null,
                null,
                packagingOptions
        );
    }
}
