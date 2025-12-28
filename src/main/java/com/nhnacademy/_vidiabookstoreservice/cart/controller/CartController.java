package com.nhnacademy._vidiabookstoreservice.cart.controller;

import com.nhnacademy._vidiabookstoreservice.cart.domain.CartOwner;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.AddCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.UpdateCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.CartResponse;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.GuestCartStatusResponse;
import com.nhnacademy._vidiabookstoreservice.cart.service.CartService;
import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    /**
     * 회원,비회원 판단 -> CartOwner 객체 만들어주는 도우미 메서드
     */
    private CartOwner resolveOwner(Long userId, Long guestId) {
        if (userId != null) {
            return CartOwner.user(userId);
        }
        if (guestId != null) {
            return CartOwner.guest(guestId);
        }
        throw new IllegalStateException("사용자 식별 정보가 없습니다. (userId, guestId 둘 다 null)");
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        Long userId = UserContext.get().getUserId();
        Long guestId = UserContext.get().getGuestId();

        return ResponseEntity.ok(cartService.getCart(resolveOwner(userId, guestId)));
    }

    // 비회원 장바구니 상태 확인용
    @GetMapping("/guest/status")
    public ResponseEntity<GuestCartStatusResponse> guestCartStatus() {
        Long guestId = UserContext.get().getGuestId();

        int itemCount = cartService.countGuestCartItems(guestId);
        boolean hasGuestCart = itemCount > 0;

        return ResponseEntity.ok(new GuestCartStatusResponse(hasGuestCart, itemCount));
    }


    @PostMapping("/items")
    public ResponseEntity<Void> addItem(@RequestBody @Valid AddCartItemRequest addItemRequest) {
        Long userId = UserContext.get().getUserId();
        Long guestId = UserContext.get().getGuestId();

        cartService.addItem(resolveOwner(userId, guestId), addItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/items/{book-id}")
    public ResponseEntity<Void> updateCartBook(
            @PathVariable("book-id") Long bookId,
            @RequestBody @Valid UpdateCartItemRequest updateCartItemRequest
    ) {
        Long userId = UserContext.get().getUserId();
        Long guestId = UserContext.get().getGuestId();

        cartService.updateItem(resolveOwner(userId, guestId), bookId, updateCartItemRequest);
        return ResponseEntity.ok().build();
    }

    /**
     * 장바구니 삭제
     */
    @DeleteMapping
    public void deleteCart() { // MySQL에서 장바구니 삭제 (회원만)
        Long userId = UserContext.get().getUserId();
        cartService.deleteCart(userId);
    }

    /**
     * 장바구니 특정 도서 삭제
     */
    @DeleteMapping("/items/{book-id}")
    public void deleteItem(
            @PathVariable("book-id") Long bookId
    ) {
        Long userId = UserContext.get().getUserId();
        Long guestId = UserContext.get().getGuestId();
        cartService.removeItem(resolveOwner(userId, guestId), bookId);
    }

    /**
     * 장바구니 아이템 여러권 삭제
     */
    @DeleteMapping("/items")
    public void deleteSelectItems(
            @RequestParam("itemIds") List<Long> bookIds
    ){
        Long userId = UserContext.get().getUserId();
        Long guestId = UserContext.get().getGuestId();

        Long id = (userId == null) ? guestId : userId;

        if(bookIds == null || bookIds.isEmpty()){
            //예외추가
        }
        cartService.removeItemByOrder(id, bookIds);
    }

    // 비회원 -> 회원 로그인 시 merge (팝업에서 "예" 눌렀을 때 호출)
    @PostMapping("/merge-guest")
    public void mergeGuestToMember(
    ) {

        Long userId = UserContext.get().getUserId();
        Long guestId = UserContext.get().getGuestId();
        cartService.mergeGuestCartToUser(guestId, userId);
    }

    // 정상 로그아웃 시 호출
    @PostMapping("/logout-sync")
    public void logoutSync() {
        Long userId = UserContext.get().getUserId();
        cartService.logoutSyncCart(userId);
        return ResponseEntity.noContent().build();
    }

    // 정상 로그인 직후 호출 (redis에 없으면 MySQL -> Redis 복원)
    @PostMapping("/login-sync")
    public void loginSync() {
        Long userId = UserContext.get().getUserId();
        cartService.loginSyncCart(userId);
    }

    // 비회원 장바구니만 삭제 (팝업에서 "아니오" 선택 시)
    @DeleteMapping("/guest")
    public void clearGuestCart(
    ) {
        Long guestId = UserContext.get().getGuestId();
        cartService.clear(CartOwner.guest(guestId));
    }

}
