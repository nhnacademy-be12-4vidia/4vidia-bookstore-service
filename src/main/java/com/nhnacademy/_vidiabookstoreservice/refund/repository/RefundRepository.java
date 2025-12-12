package com.nhnacademy._vidiabookstoreservice.refund.repository;

import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.dto.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    Page<Refund> findAllByRefundStatus(RefundStatus status, Pageable pageable);
}
