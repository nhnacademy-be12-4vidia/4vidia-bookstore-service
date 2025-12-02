package com.nhnacademy._vidiabookstoreservice.point.repository;

import com.nhnacademy._vidiabookstoreservice.point.domain.PointPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointPolicyRepository extends JpaRepository<PointPolicy, Long> {
}
