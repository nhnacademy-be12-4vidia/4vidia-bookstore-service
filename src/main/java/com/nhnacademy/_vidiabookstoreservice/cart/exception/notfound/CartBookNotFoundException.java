package com.nhnacademy._vidiabookstoreservice.cart.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class CartBookNotFoundException extends NotFoundException {
    public CartBookNotFoundException(Long cartBookId) {
        super("아이디:%d 장바구니 아이템을 찾을 수 없습니다.".formatted(cartBookId));
    }
}
