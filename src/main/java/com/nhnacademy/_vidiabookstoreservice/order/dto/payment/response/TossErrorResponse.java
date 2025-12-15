package com.nhnacademy._vidiabookstoreservice.order.dto.payment.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossErrorResponse(
        String code,
        String message
) {
}
