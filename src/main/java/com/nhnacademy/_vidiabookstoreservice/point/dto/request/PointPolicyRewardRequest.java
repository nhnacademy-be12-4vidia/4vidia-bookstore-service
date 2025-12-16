package com.nhnacademy._vidiabookstoreservice.point.dto.request;

import jakarta.validation.constraints.NotNull;

public record PointPolicyRewardRequest(
        @NotNull
        Long userId,
        @NotNull
        Long policyId
) {
}
