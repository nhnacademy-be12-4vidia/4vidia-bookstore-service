package com.nhnacademy._vidiabookstoreservice.order.repository;

import com.nhnacademy._vidiabookstoreservice.order.domain.PackagingOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackagingOptionRepository extends JpaRepository<PackagingOption, Long> {
}
