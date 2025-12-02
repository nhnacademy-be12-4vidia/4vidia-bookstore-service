package com.nhnacademy._vidiabookstoreservice.point.repository;


import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointDetailRepository extends JpaRepository<PointDetail, Long> {



}
