package com.nhnacademy._vidiabookstoreservice.order.repository;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(Long orderId);

    List<Order> findAllByUser_UserId(Long userUserId);

    @Query("select distinct o from Order o " +
            "join fetch o.orderItems oi " +
            "join fetch oi.book " +
            "where o.orderId = :orderId")
    Optional<Order> findByOrderIdWithAll(Long orderId);
}
