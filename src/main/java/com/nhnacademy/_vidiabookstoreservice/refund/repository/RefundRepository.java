package com.nhnacademy._vidiabookstoreservice.refund.repository;

import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    Page<Refund> findAllByRefundStatus(RefundStatus status, Pageable pageable);

    @Query("""
        select r
        from Refund r
        join r.orderItem oi
        join oi.order o
        where o.user.userId = :userId
    """)
    List<Refund> findAllByUserId(@Param("userId") Long userId);

    @Query("""
        select r
        from Refund r
        join r.orderItem oi
        join oi.order o
        where o.user.userId = :userId
          and r.refundStatus = :status
    """)
    List<Refund> findAllByUserIdAndRefundStatus(
            @Param("userId") Long userId,
            @Param("status") RefundStatus status
    );

    /**
     * 반품 처리 된 도서 수량
     */
    @Query("""
        select coalesce(sum(oi.quantity), 0)
        from Refund r
        join r.orderItem oi
        where oi.order.orderId = :orderId
          and r.refundStatus = :refundStatus
    """)
    int sumRefundedQuantity(@Param("orderId") Long orderId,
                            @Param("refundStatus") RefundStatus refundStatus);

}
