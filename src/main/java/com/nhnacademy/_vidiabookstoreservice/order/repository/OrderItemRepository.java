package com.nhnacademy._vidiabookstoreservice.order.repository;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    Optional<OrderItem> findByOrderItemId(Long orderItemId);

    List<OrderItem> findByOrder(Order order);
    List<OrderItem> findAllByConfirmStatusAndOrder(ConfirmStatus confirmStatus, Order order);

    @Query("""
    select sum(oi.quantity)
    from OrderItem oi
    where oi.order.orderId = :orderId
""")
    Integer sumOrderItemQuantity(@Param("orderId") Long orderId);

    /**
     * 구매 확정 -> 순수 주문 금액 계산
     */
    @Query("""
        select COALESCE(SUM(oi.salePrice * oi.quantity), 0) - o.couponDiscount
        from OrderItem oi
        join oi.order o
        where o.orderId = :orderId
          and oi.confirmStatus = :confirmStatus
    """)
    int calculateNetOrderPrice(@Param("orderId") Long orderId,
                               @Param("confirmStatus")ConfirmStatus confirmStatus);
}
