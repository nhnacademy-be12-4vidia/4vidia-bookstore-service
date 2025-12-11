package com.nhnacademy._vidiabookstoreservice.refund.repository;

import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}
