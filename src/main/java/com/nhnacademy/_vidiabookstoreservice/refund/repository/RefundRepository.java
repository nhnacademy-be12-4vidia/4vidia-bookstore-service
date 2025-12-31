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

    @Query(value = """
            select r
            from Refund r
            join fetch r.order o
            where o.user.userId = :userId
            and (:status is null or r.refundStatus = :status)
            order by r.createdAt desc
        """,
            countQuery = """
            select count(r)
            from Refund r
            join r.order o
            where o.user.userId = :userId
            and (:status is null or r.refundStatus = :status)
    """)
    Page<Refund> findMyRefunds(
            @Param("userId") Long userId,
            @Param("status") RefundStatus status,
            Pageable pageable
    );

    /**
     * 상태별 카운트를 한 번의 쿼리로 가져오기 (성능 최적화)
     */
    @Query("""
            select r.refundStatus, count(r)
            from Refund r
            where r.order.user.userId = :userId
            group by r.refundStatus
        """)
    List<Object[]> countByUserGroupByStatus(@Param("userId") Long userId);


}
