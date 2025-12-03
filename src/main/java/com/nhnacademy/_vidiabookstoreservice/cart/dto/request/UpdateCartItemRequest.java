package com.nhnacademy._vidiabookstoreservice.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(
        @NotNull
        @Min(value = 1)
        Integer quantity
) {}

