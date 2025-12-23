package com.nhnacademy._vidiabookstoreservice.user.dto.event;

import java.time.LocalDateTime;

public record CouponIssueEvent(
        Long userId,
        Long policyId,
        LocalDateTime issuedAt,
        LocalDateTime expireAt
) {
}
