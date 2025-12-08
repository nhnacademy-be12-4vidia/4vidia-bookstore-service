package com.nhnacademy._vidiabookstoreservice.order.service;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderItemRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderItemResponse;

import java.util.List;

public interface OrderItemService{

    OrderItem addOrderItem(OrderItem orderItem);

    void changeStatusOrderItem(Long orderItemId, ConfirmStatus confirmStatus);

    OrderItemResponse getByOrderItemId(Long orderItemId);

    OrderItem getProxyById(Long orderItemId);

    List<OrderItemRequest> getOrderItemRequests(Order order);
}
