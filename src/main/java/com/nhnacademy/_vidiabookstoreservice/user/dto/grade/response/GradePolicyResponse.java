package com.nhnacademy._vidiabookstoreservice.user.dto.grade.response;

import lombok.Builder;

@Builder
public record GradePolicyResponse(
        String gradeName,
        Integer pointRate,
        Long minNetAmount,
        Long maxNetAmount, // 마지막 등급이면 null
        String desc
) {}
