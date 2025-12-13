package com.nhnacademy._vidiabookstoreservice.order.domain;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutRequest;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class CheckoutSession {
    public List<OrderCheckoutRequest> orderCheckoutList;
}
