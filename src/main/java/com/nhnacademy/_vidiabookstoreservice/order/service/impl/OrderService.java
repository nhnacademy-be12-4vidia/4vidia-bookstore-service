package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy.order.domain.Order;
import com.nhnacademy.order.domain.OrderStatus;
import com.nhnacademy.order.domain.dto.OrderCreateRequest;
import com.nhnacademy.order.domain.dto.OrderResponse;
import com.nhnacademy.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderResponse saveOrder(OrderCreateRequest request) {
        Long userId = 1L; //TODO userId 꺼내기
        Order order = Order.builder()
                .userId(userId)
                .recipientName(request.recipientName())
                .addressRoadname(request.addressRoadname())
                .addressDetail(request.addressDetail())
                .zipCode(request.zipCode())
                .recipientPhone(request.recipientPhone())
                .deliveryRequest(request.deliveryRequest())
                .couponDiscount(request.couponDiscount())
                .pointUsed(request.pointUsed())
                .deliveryDate(request.deliveryDate())
                .totalPrice(request.totalPrice())
                .payPrice(request.payPrice())
                .orderItems(request.orderItems())
                .build();
        //TODO 재고 확인 구현 wow

        orderRepository.save(order);

        return new OrderResponse(order.getOrderId(), order.getUserId(), order.getRecipientName(),
                order.getAddressRoadname(), order.getAddressDetail(), order.getZipCode(),
                order.getRecipientPhone(), order.getDeliveryRequest(), order.getCreatedAt(),
                order.getCouponDiscount(), order.getPointUsed(), order.getDeliveryDate(),
                order.getDeliveryStatus(), order.getActualDeliveryDate(), order.getTotalPrice(), order.getPayPrice());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        return new OrderResponse(order.getOrderId(), order.getUserId(), order.getRecipientName(),
                order.getAddressRoadname(), order.getAddressDetail(), order.getZipCode(),
                order.getRecipientPhone(), order.getDeliveryRequest(), order.getCreatedAt(),
                order.getCouponDiscount(), order.getPointUsed(), order.getDeliveryDate(),
                order.getDeliveryStatus(), order.getActualDeliveryDate(), order.getTotalPrice(), order.getPayPrice());
    }

    public void updateOrderStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        order.setOrderStatus(OrderStatus.PAID);
    }
}
