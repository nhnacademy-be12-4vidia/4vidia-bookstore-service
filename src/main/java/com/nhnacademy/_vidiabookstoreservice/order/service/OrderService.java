package com.nhnacademy._vidiabookstoreservice.order.service;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.OrderStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.*;

import java.util.List;

public interface OrderService {

    List<DeliveryDateResponse> getDeliveryDates();

    OrderResponse getOrderResponse(Long orderItemId);

    OrderCreateResponse saveOrder(Long userId, OrderCreateRequest request);

    Order getOrder(Long orderId);

    void updateOrderStatus(Long orderId, OrderStatus orderStatus);

    List<OrderPreviewResponse> getOrdersByUserId(Long userId);

    OrderCheckoutResponse getOrderCheckoutResponse(Long userId, List<OrderCheckoutRequest> orderCheckoutRequests);
}
