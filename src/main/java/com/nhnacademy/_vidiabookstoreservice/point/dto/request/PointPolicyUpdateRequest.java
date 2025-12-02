package com.nhnacademy._vidiabookstoreservice.point.dto.request;

import jakarta.validation.constraints.Min;

public record PointPolicyUpdateRequest(
        @Min(1)
        Integer price
) {}
