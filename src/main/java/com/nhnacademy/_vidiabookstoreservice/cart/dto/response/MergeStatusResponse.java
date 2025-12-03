package com.nhnacademy._vidiabookstoreservice.cart.dto.response;

public record MergeStatusResponse (
        boolean hasGuestCart,
        int guestItemCount,
        boolean hasUserCart,
        int userItemCount
){
}
