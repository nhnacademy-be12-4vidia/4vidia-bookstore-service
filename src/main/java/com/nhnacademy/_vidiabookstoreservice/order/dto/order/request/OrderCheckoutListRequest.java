package com.nhnacademy._vidiabookstoreservice.order.dto.order.request;

import java.util.List;

public record OrderCheckoutListRequest(
    List<OrderCheckoutRequest> items
) { }