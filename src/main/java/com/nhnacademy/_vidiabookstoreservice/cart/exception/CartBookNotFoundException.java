package com.nhnacademy._vidiabookstoreservice.cart.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class CartBookNotFoundException extends BaseException {
    public CartBookNotFoundException(Long userId, Long bookId) {
        super(CartErrorCode.CART_BOOK_NOT_FOUND, "회원 아이디 : %d, 도서 아이디:%d".formatted(userId, bookId));
    }
}
