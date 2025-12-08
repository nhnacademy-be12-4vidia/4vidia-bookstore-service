package com.nhnacademy._vidiabookstoreservice.admin.dto.response;

import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;

public record GradePolicyResponse(
        Long gradeId,
        String gradeName,
        Integer pointRate
) {
    public static GradePolicyResponse from (Grade grade){
        return new GradePolicyResponse(
                grade.getGradeId(),
                grade.getGradeName().name(),
                grade.getPointRate()
        );
    }
}
