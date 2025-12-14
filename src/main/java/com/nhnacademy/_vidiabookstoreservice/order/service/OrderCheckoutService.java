package com.nhnacademy._vidiabookstoreservice.order.service;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderCheckoutResponse;

import java.util.List;

public interface OrderCheckoutService {

    String initiateCheckout (List<OrderCheckoutRequest> orderCheckoutRequests);

    OrderCheckoutResponse getOrderCheckoutResponse(Long userId, String key);
}
