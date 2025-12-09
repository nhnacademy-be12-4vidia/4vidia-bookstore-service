package com.nhnacademy._vidiabookstoreservice.cart.service;


import com.nhnacademy._vidiabookstoreservice.cart.domain.CartOwner;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.AddCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.UpdateCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.CartResponse;

import java.util.List;

public interface CartService {
    // 기본 장바구니
    CartResponse getCart(CartOwner cartOwner);
    void addItem(CartOwner owner, AddCartItemRequest addItemRequest);
    void updateItem(CartOwner owner, Long bookId, UpdateCartItemRequest updateRequest);
    void removeItem(CartOwner owner, Long bookId);
    void removeItemByOrder(Long userId, List<Long> orderBooks);
    void clear(CartOwner owner);
    void deleteCart(Long userId);

    // 비회원 → 회원 머지
    int countGuestCartItems(Long guestId);
    void mergeGuestCartToUser(Long guestId, Long userId);

    // Redis ↔ MySQL 동기화 / 로그인·로그아웃
    void flushCartFromRedisToMySql(Long userId);
    void logoutCart(Long userId);
    void loginSyncCart(Long userId);
}

