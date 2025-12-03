package com.nhnacademy._vidiabookstoreservice.cart.dto.response;

import java.util.List;

public record CartResponse(
        Long userId,
        List<CartBookResponse> items
) {
}

