package com.nhnacademy._vidiabookstoreservice.cart.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class CartNotFoundException extends NotFoundException {
    public CartNotFoundException(Long userId) {
        super("유저:%d의 장바구니를 찾을 수 없습니다.".formatted(userId));
    }
}
