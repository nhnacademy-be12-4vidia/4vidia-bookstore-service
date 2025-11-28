package com.nhnacademy._vidiabookstoreservice.order.service;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.OrderStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderResponse;

public interface OrderService {

    OrderResponse getOrderResponse(Long orderItemId);

    Long saveOrder(OrderCreateRequest request);

    Order getOrder(Long orderId);

    void updateOrderStatus(Long orderId, OrderStatus orderStatus);
}
