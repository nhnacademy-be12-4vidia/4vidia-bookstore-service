package com.nhnacademy._vidiabookstoreservice.cart.domain;

import com.nhnacademy._vidiabookstoreservice.cart.domain.enums.CartOwnerType;

public record CartOwner (CartOwnerType type, String id)
{
    public static CartOwner user(Long userId){
        return new CartOwner(CartOwnerType.USER, String.valueOf(userId));
    }

    public static CartOwner guest(String guestId){
        return new CartOwner(CartOwnerType.GUEST, guestId);
    }

    public boolean isUser(){
        return type == CartOwnerType.USER;
    }
}
