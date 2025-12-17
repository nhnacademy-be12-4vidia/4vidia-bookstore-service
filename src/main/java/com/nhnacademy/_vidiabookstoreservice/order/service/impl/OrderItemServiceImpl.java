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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Override
    public OrderItem addOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    @Override
    public void changeStatusOrderItem_byUser(Long orderItemId, ConfirmStatus confirmStatus) {
        OrderItem findOrderItem = orderItemRepository.findByOrderItemId(orderItemId).orElseThrow(
                () -> new OrderItemNotFoundException(orderItemId));

        if (findOrderItem.getConfirmStatus().equals(ConfirmStatus.UNCONFIRMED)) {
            //미확정인것만 확정 상태로 변경, 반품 완료, 반품 신청 중인 것도 제외
            findOrderItem.setConfirmStatus(confirmStatus);
        }
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
    @Transactional(readOnly = true)
    public List<OrderItemRequest> getOrderItemRequests(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);

        List<OrderItemRequest> orderItemRequests = orderItems.stream()
                .map(OrderItemRequest::fromOrder)
                .toList();
        return orderItemRequests;
    }
}
