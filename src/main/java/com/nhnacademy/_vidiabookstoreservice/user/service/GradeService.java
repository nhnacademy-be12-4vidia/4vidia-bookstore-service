package com.nhnacademy._vidiabookstoreservice.user.service;


import com.nhnacademy._vidiabookstoreservice.user.dto.grade.response.GradePolicyResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.grade.response.GradeResponse;

import java.util.List;

public interface GradeService {
    GradeResponse getGrade(Long userId);
    void updateGrade(Long userId, Long gradeId/*Grade grade*/);
    int recalculateMonthlyGrades();
    List<GradePolicyResponse> getGradePolicies();
}
