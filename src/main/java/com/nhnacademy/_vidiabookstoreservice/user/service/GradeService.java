package com.nhnacademy._vidiabookstoreservice.user.service;


import com.nhnacademy._vidiabookstoreservice.user.dto.grade.response.GradeResponse;

public interface GradeService {
    GradeResponse getGrade(Long userId);
    void updateGrade(Long userId, Long gradeId/*Grade grade*/);
}
