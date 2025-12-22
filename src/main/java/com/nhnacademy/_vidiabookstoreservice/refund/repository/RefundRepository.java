package com.nhnacademy._vidiabookstoreservice.refund.repository;

import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    Page<Refund> findAllByRefundStatus(RefundStatus status, Pageable pageable);

    @Query("""
        select distinct r
        from Refund r
        join r.refundItems ri
        join ri.orderItem oi
        join oi.order o
        where o.user.userId = :userId
    """)
    List<Refund> findAllByUserId(@Param("userId") Long userId);

    @Query("""
        select distinct r
        from Refund r
        join r.refundItems ri
        join ri.orderItem oi
        join oi.order o
        where o.user.userId = :userId
          and r.refundStatus = :status
    """)
    List<Refund> findAllByUserIdAndRefundStatus(
            @Param("userId") Long userId,
            @Param("status") RefundStatus status
    );
}
