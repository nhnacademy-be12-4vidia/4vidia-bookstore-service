package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.exception.InvalidOrderStateException;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminDeliveryService;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminDeliveryServiceImpl implements AdminDeliveryService {
    private final OrderRepository orderRepository;

    @Override
    public Page<Order> listByDeliveryStatus(DeliveryStatus status, String keyword, Pageable pageable) {

        String trimmed = (keyword == null || keyword.trim().isEmpty())
                ? null
                : keyword.trim();
        // 검색+상태 필터를 다 지원하는 쿼리 사용
        return orderRepository.searchAdminDeliveries(status, trimmed, pageable);
    }


    @Override
    public Order startDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getDeliveryStatus() != DeliveryStatus.WAITING) {
            throw new InvalidOrderStateException("배송 시작은 WAITING 상태에서만 가능합니다. 현재 상태: %s".formatted(order.getDeliveryStatus()));
        }

        order.setDeliveryStatus(DeliveryStatus.SHIPPING);
        order.setActualDeliveryDate(LocalDate.now()); // 배송 시작일
        return order;
    }

    @Override
    public Order completeDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        if (order.getDeliveryStatus() != DeliveryStatus.SHIPPING) {
            throw new InvalidOrderStateException("배송 완료는 SHIPPING 상태에서만 가능합니다. 현재 상태: %s"
                    .formatted(order.getDeliveryStatus())
            );
        }

        order.setDeliveryStatus(DeliveryStatus.DELIVERED);
        return order;
    }


}
