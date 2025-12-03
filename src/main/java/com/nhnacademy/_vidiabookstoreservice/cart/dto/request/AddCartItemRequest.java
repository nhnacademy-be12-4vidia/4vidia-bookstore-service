package com.nhnacademy._vidiabookstoreservice.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @NotNull
        Long bookId,
        @NotNull
        @Min(1)
        Integer quantity) {
}
