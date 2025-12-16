package com.nhnacademy._vidiabookstoreservice.admin.repository;

import com.nhnacademy._vidiabookstoreservice.admin.domain.PointPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointPolicyRepository extends JpaRepository<PointPolicy, Long> {
    Optional<PointPolicy> findByPointPolicyId(Long pointPolicyId);
}
