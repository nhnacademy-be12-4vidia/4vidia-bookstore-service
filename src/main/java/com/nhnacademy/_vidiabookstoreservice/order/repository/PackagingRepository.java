package com.nhnacademy._vidiabookstoreservice.order.repository;

import com.nhnacademy._vidiabookstoreservice.order.domain.Packaging;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackagingRepository extends JpaRepository<Packaging, Long> {
}
