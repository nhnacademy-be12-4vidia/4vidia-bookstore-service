package com.nhnacademy._vidiabookstoreservice.order.service;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.OrderStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.DeliveryDateResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderPreviewResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderResponse;

import java.util.List;

public interface OrderService {

    List<DeliveryDateResponse> getDeliveryDates();

    OrderResponse getOrderResponse(Long orderItemId);

    Long saveOrder(Long userId, OrderCreateRequest request);

    Order getOrder(Long orderId);

    void updateOrderStatus(Long orderId, OrderStatus orderStatus);

    List<OrderPreviewResponse> getOrdersByUserId(Long userId);
}
