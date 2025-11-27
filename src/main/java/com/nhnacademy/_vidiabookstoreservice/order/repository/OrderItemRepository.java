package com.nhnacademy._vidiabookstoreservice.order.repository;

import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
