package com.nhnacademy._vidiabookstoreservice.refund.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.refund.dto.OrderItemResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.RefundResponse;
import com.nhnacademy._vidiabookstoreservice.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public RefundResponse getRefundList(long orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        List<OrderItem> orderItems =  orderItemRepository.findAllByConfirmStatusAndOrder(ConfirmStatus.UNCONFIRMED,order);

        List<OrderItemResponse> response = orderItems.stream().map(OrderItemResponse::from).toList();

        return new RefundResponse(orderId, response);
    }
}
