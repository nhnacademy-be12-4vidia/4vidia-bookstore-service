package com.nhnacademy._vidiabookstoreservice.user.repository;

import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade,Long> {
    Grade findByGradeName(GradeName gradeName);
}
