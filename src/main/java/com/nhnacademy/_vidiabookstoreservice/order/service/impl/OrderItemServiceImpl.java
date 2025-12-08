package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderItemRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderItemResponse;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderItemNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;


    @Override
    public OrderItem addOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    @Override
    public void changeStatusOrderItem(Long orderItemId, ConfirmStatus confirmStatus) {
        OrderItem findOrderItem = orderItemRepository.findByOrderItemId(orderItemId).orElseThrow(
                () -> new OrderItemNotFoundException(orderItemId));

        findOrderItem.setConfirmStatus(confirmStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItemResponse getByOrderItemId(Long orderItemId) {

        OrderItem orderItem = orderItemRepository.findByOrderItemId(orderItemId).orElseThrow(
                () -> new OrderItemNotFoundException(orderItemId));

        return OrderItemResponse.from(orderItem);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItem getProxyById(Long orderItemId) {
        return orderItemRepository.getReferenceById(orderItemId);
    }

    @Override
    public List<OrderItemRequest> getOrderItemRequests(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);

        List<OrderItemRequest> orderItemRequests = orderItems.stream()
                .map(OrderItemRequest::fromOrder)
                .toList();
        return orderItemRequests;
    }
}
