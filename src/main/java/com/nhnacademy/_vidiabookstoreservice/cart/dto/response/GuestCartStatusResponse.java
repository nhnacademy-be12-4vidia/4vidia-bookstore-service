package com.nhnacademy._vidiabookstoreservice.cart.dto.response;

public record GuestCartStatusResponse(
        boolean hasGuestCart,
        int itemCount
) {}
