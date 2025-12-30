package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderItemRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderItemResponse;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.OrderItemNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderItemService;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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

        return orderItems.stream()
                .map(OrderItemRequest::fromOrder)
                .toList();
    }

    private static final int AUTO_CONFIRM_DAYS = 30;
    @Override
    public int autoConfirmDeliveredOrderItems(){
        LocalDate cutoff = LocalDate.now().minusDays(AUTO_CONFIRM_DAYS);

        List<Long> targetIds = orderItemRepository.findAutoConfirmTargetItemIds(
                cutoff,
                DeliveryStatus.DELIVERED,
                ConfirmStatus.UNCONFIRMED,
                RefundStatus.PROCESS
        );

        if(targetIds.isEmpty()){
            return 0;
        }
        int updated = orderItemRepository.bulkConfirmByIds(targetIds,ConfirmStatus.CONFIRMED);
        log.info("자동 구매확정 처리 완료: {}건 (cutoff={})",updated,cutoff);
        return updated;
    }
}
