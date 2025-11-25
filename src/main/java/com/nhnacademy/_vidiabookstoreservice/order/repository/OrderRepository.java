package com.nhnacademy._vidiabookstoreservice.order.repository;

import com.nhnacademy.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
