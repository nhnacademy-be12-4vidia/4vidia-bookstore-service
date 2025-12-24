package com.nhnacademy._vidiabookstoreservice.refund.repository;

import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundItemStatus;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RefundItemRepository extends JpaRepository<RefundItem, Long> {
    boolean existsByOrderItem_OrderItemId(Long orderItemId);

    // 특정 주문 전체 환불 금액 합계
    @Query("SELECT COALESCE(SUM(ri.refundPrice), 0) " +
            "FROM RefundItem ri " +
            "WHERE ri.orderItem.order.orderId = :orderId")
    int sumRefundedPriceByOrder(@Param("orderId") Long orderId);

    // 특정 주문 + 카테고리 환불 금액 합계
    @Query("SELECT COALESCE(SUM(ri.refundPrice), 0) " +
            "FROM RefundItem ri " +
            "WHERE ri.orderItem.order.orderId = :orderId " +
            "AND ri.orderItem.book.category.kdcCode = :categoryKdcId")
    int sumCategoryRefundedPriceByOrderAndCategory(@Param("orderId") Long orderId,
                                                   @Param("categoryKdcId") String categoryKdcId);

    @Query("SELECT ri FROM RefundItem ri WHERE ri.refund.refundId = :refundId")
    List<RefundItem> findAllByRefund_RefundId(Long refundId);


    @Query("""
        SELECT r FROM RefundItem r WHERE r.orderItem.orderItemId in :ordersItemIds
        """)
    List<RefundItem> findByOrderItem_OrderItemId(@Param("ordersItemIds") List<Long> ordersItemIds);

    boolean existsByRefund_RefundIdAndRefundItemStatus(
            Long refundId,
            RefundItemStatus refundStatus
    );
}
