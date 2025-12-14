package com.nhnacademy._vidiabookstoreservice.order.domain;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSession {
    public List<OrderCheckoutRequest> orderCheckoutList;
}
