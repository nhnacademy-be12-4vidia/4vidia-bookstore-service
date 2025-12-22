package com.nhnacademy._vidiabookstoreservice.order.repository;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    Optional<OrderItem> findByOrderItemId(Long orderItemId);

    List<OrderItem> findByOrder(Order order);

    @Query("SELECT oi FROM OrderItem oi " +
            "LEFT JOIN RefundItem ri ON ri.orderItem = oi " +
            "WHERE oi.order.orderId = :orderId " +
            "AND ri.refundItemId IS NULL")
    List<OrderItem> findNotRefundedOrderItems(@Param("orderId") Long orderId);

    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.book WHERE oi.order.orderId = :orderId")
    List<OrderItem> findByOrder_orderId(@Param("orderId") Long orderId);


    @Query("""
        SELECT COALESCE(SUM(oi.salePrice * oi.quantity), 0)
        FROM OrderItem oi
        WHERE oi.order.orderId = :orderId
          AND oi.book.category.kdcCode = :kdcCode
        """)
    int sumCategoryItemPrice(
            @Param("orderId") Long orderId,
            @Param("kdcCode") String kdcCode
    );
}
