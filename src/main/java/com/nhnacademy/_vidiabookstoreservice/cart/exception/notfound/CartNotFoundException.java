package com.nhnacademy._vidiabookstoreservice.cart.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.cart.exception.CartErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class CartNotFoundException extends BaseException {
    public CartNotFoundException(Long userId) {
        super(CartErrorCode.CART_NOT_FOUND," 유저 아이디: %d".formatted(userId));
    }
}
