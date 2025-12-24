package com.nhnacademy._vidiabookstoreservice.order.service;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderTrackingRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.*;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentConfirmRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    OrderResponse getOrderResponse(Long orderItemId);

    OrderCreateResponse saveOrder(Long userId, OrderCreateRequest request);

    PaymentResponse payAndCompleteOrder(Long orderId, PaymentConfirmRequest confirmRequest, Long userId);

    Order getOrder(Long orderId);

    Page<OrderPreviewResponse> getOrdersByUserId(Long userId, String status, Pageable pageable);
    OrderCountResponse getOrderCounts(Long userId);

    void cancelOrderIfPending(Long orderId);

    void cancelOrder(Long orderId, String message);

    Boolean validateGuest(OrderTrackingRequest orderTrackingRequest);

    void changeOrderStatus(Long orderId, ConfirmStatus confirmStatus);

    void changeOrderStatus_ByUser(Long orderId, ConfirmStatus confirmStatus);
}
