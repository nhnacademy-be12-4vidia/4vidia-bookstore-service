package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotEnoughException;

public class BookStockNotEnoughException extends NotEnoughException {

    public BookStockNotEnoughException() {
        super("현재 재고가 부족합니다.");
    }
}
