package com.nhnacademy._vidiabookstoreservice.order.service;

import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderItemResponse;

public interface OrderItemService{

    OrderItem addOrderItem(OrderItem orderItem);

    void confirmOrderItem(OrderItem orderItem);

    OrderItemResponse getByOrderItemId(Long orderItemId);

    OrderItem getProxyById(Long orderItemId);

}
