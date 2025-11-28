package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
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
    @Transactional(readOnly = true)
    public OrderItemResponse getByOrderItemId(Long orderItemId) {

        OrderItem orderItem = orderItemRepository.findByOrderItemId(orderItemId).orElseThrow(
                () -> new OrderItemNotFoundException("ID에 해당하는 주문아이템을 찾을 수 없습니다. ID: %d".formatted(orderItemId)));

        return OrderItemResponse.from(orderItem);
    }

    @Override
    public OrderItem getProxyById(Long orderItemId) {
        return orderItemRepository.getReferenceById(orderItemId);
    }
}
