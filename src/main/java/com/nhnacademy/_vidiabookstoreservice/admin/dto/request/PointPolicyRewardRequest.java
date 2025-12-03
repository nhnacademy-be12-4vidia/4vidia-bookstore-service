package com.nhnacademy._vidiabookstoreservice.admin.dto.request;

public record PointPolicyRewardRequest(
        Long orderId,
        Long policyId,
        int amount
) {
}
