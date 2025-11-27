package com.nhnacademy._vidiabookstoreservice.order.service;

import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;

public interface OrderItemService {

    OrderItem getByOrderItemId(Long orderItemId);

}
