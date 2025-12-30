package com.nhnacademy._vidiabookstoreservice.order.repository;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    Optional<OrderItem> findByOrderItemId(Long orderItemId);

    List<OrderItem> findByOrder(Order order);

    @Query("SELECT DISTINCT oi FROM OrderItem oi " +
            "LEFT JOIN FETCH oi.refundItems " +
            "WHERE oi.order.orderId = :orderId")
    List<OrderItem> findAllByOrderIdWithRefunds(@Param("orderId") Long orderId);

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

    @Query("""
    select oi.orderItemId
    from OrderItem oi
    join oi.order o
    where o.actualDeliveryDate is not null
      and o.actualDeliveryDate <= :cutoffDate
      and o.deliveryStatus = :delivered
      and oi.confirmStatus = :unconfirmed
      and not exists (
          select 1
          from Refund r
          where r.order = o
            and r.refundStatus = :refundProcess
      )
    """)
    List<Long> findAutoConfirmTargetItemIds(
            @Param("cutoffDate") LocalDate cutoffDate,
            @Param("delivered") DeliveryStatus delivered,
            @Param("unconfirmed") ConfirmStatus unconfirmed,
            @Param("refundProcess") RefundStatus refundProcess
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update OrderItem oi
    set oi.confirmStatus = :confirmed
    where oi.orderItemId in :ids
    """)
    int bulkConfirmByIds(
            @Param("ids") List<Long> ids,
            @Param("confirmed") ConfirmStatus confirmed
    );

}
