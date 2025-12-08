package com.nhnacademy._vidiabookstoreservice.admin.dto.request;

import jakarta.validation.constraints.Min;

public record GradePolicyUpdateRequest(
        @Min(1)
        Integer pointRate
) {}
