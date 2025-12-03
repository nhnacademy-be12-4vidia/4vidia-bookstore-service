package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderItemResponse;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderItemNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;


    @Override
    public void addOrderItem(OrderItem orderItem) {
        orderItemRepository.save(orderItem);
    }

    @Override
    public void confirmOrderItem(OrderItem orderItem) {
        OrderItem findOrderItem = orderItemRepository.findByOrderItemId(orderItem.getOrderItemId()).orElseThrow(
                () -> new OrderItemNotFoundException(orderItem.getOrderItemId()));
        findOrderItem.setConfirmStatus(ConfirmStatus.CONFIRMED);
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
}
