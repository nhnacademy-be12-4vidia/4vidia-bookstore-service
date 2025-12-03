package com.nhnacademy._vidiabookstoreservice.admin.dto.request;

import jakarta.validation.constraints.Min;

public record PointPolicyUpdateRequest(
        @Min(1)
        Integer price
) {}
