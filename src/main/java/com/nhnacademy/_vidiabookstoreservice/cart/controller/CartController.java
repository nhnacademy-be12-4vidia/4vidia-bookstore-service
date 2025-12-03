package com.nhnacademy._vidiabookstoreservice.cart.controller;

import com.nhnacademy._vidiabookstoreservice.cart.domain.CartOwner;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.AddCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.UpdateCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.CartResponse;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.GuestCartStatusResponse;
import com.nhnacademy._vidiabookstoreservice.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    /**
     * 회원,비회원 판단 -> CartOwner 객체 만들어주는 도우미 메서드
     */
    private CartOwner resolveOwner(Long userId, String guestId) {
        if (userId != null) {
            return CartOwner.user(userId);
        }
        if (guestId != null) {
            return CartOwner.guest(guestId);
        }
        throw new IllegalStateException("사용자 식별 정보가 없습니다. (userId, guestId 둘 다 null)");
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Guest-Id", required = false) String guestId
    ) {
        return ResponseEntity.ok(cartService.getCart(resolveOwner(userId, guestId)));
    }

    // 비회원 장바구니 상태 확인용
    @GetMapping("/guest/status")
    public ResponseEntity<GuestCartStatusResponse> guestCartStatus(
            @RequestHeader(value = "X-Guest-Id", required = false) String guestId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        int itemCount = cartService.countGuestCartItems(guestId);
        boolean hasGuestCart = itemCount > 0;

        return ResponseEntity.ok(new GuestCartStatusResponse(hasGuestCart, itemCount));
    }


    @PostMapping("/items")
    public ResponseEntity<Void> addItem(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Guest-Id", required = false) String guestId,
            @RequestBody @Valid AddCartItemRequest addItemRequest
    ) {
        cartService.addItem(resolveOwner(userId, guestId), addItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/items/{bookId}")
    public ResponseEntity<Void> updateCartBook(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Guest-Id", required = false) String guestId,
            @PathVariable Long bookId,
            @RequestBody @Valid UpdateCartItemRequest updateCartItemRequest
    ) {
        cartService.updateItem(resolveOwner(userId, guestId), bookId, updateCartItemRequest);
        return ResponseEntity.ok().build();
    }

    // 장바구니 삭제
    @DeleteMapping
    public ResponseEntity<Void> deleteCart(@RequestHeader("X-User-Id") Long userId) { // MySQL에서 장바구니 삭제 (회원만)
        cartService.deleteCart(userId);
        return ResponseEntity.noContent().build();
    }

    // 장바구니 비우기
    @DeleteMapping("/items")
    public ResponseEntity<Void> clearCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Guest-Id", required = false) String guestId
    ) {
        cartService.clear(resolveOwner(userId, guestId));
        return ResponseEntity.noContent().build();
    }

    // 장바구니 특정 도서 삭제
    @DeleteMapping("/items/{bookId}")
    public ResponseEntity<Void> deleteItem(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Guest-Id", required = false) String guestId,
            @PathVariable Long bookId
    ) {
        cartService.removeItem(resolveOwner(userId, guestId), bookId);
        return ResponseEntity.noContent().build();
    }

    // 비회원 -> 회원 로그인 시 merge (팝업에서 "예" 눌렀을 때 호출)
    @PostMapping("/merge-guest")
    public ResponseEntity<Void> mergeGuestToMember(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Guest-Id") String guestId
    ) {
        cartService.mergeGuestCartToUser(guestId, userId);
        return ResponseEntity.ok().build();
    }

    // 정상 로그아웃 시 호출
    @PostMapping("/logout-sync")
    public ResponseEntity<Void> logoutSync(@RequestHeader("X-User-Id") Long userId) {
        cartService.logoutCart(userId);
        return ResponseEntity.noContent().build();
    }

    // 정상 로그인 직후 호출 (MySQL -> Redis 복원)
    @PostMapping("/login-sync")
    public ResponseEntity<Void> loginSync(@RequestHeader("X-User-Id") Long userId) {
        cartService.loginSyncCart(userId);
        return ResponseEntity.noContent().build();
    }

    // 비회원 장바구니만 삭제 (팝업에서 "아니오" 선택 시)
    @DeleteMapping("/guest")
    public ResponseEntity<Void> clearGuestCart(
            @RequestHeader("X-Guest-Id") String guestId
    ) {
        cartService.clear(CartOwner.guest(guestId));
        return ResponseEntity.noContent().build();
    }

}
