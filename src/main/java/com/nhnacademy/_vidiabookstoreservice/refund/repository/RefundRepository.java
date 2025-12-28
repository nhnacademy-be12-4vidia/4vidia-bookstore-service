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

    // TODO 변경 예정 (일단 기능 구현부터)
    @Query("select distinct r from Refund r " +
            "join fetch r.refundItems ri " +
            "join fetch ri.orderItem oi " +
            "join fetch oi.book " +
            "join fetch r.order " +
            "where r.order.user.userId = :userId " +
            "and (:status is null or r.refundStatus = :status) " +
            "order by r.createdAt desc")
    Page<Refund> findAllByUserIdAndStatusWithDetails(
            @Param("userId") Long userId,
            @Param("status") RefundStatus status,
            Pageable pageable
    );


    // 전체 반품 개수
    long countByOrder_User_UserId(Long userId);

    // 상태별 반품 개수
    long countByOrder_User_UserIdAndRefundStatus(Long userId, RefundStatus status);


}
