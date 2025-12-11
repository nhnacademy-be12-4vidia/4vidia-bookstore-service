package com.nhnacademy._vidiabookstoreservice.admin.service;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminDeliveryService {
    Page<Order> listByDeliveryStatus(DeliveryStatus status,String keyword, Pageable pageable);
    Order startDelivery(Long orderId);
    Order completeDelivery(Long orderId);
}
