package com.nhnacademy._vidiabookstoreservice.point.dto.request;

public record PointPolicyRewardRequest(
        Long orderId,
        Long policyId,
        int amount
) {
}
