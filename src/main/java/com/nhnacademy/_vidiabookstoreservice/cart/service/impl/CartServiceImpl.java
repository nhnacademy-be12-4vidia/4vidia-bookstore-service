package com.nhnacademy._vidiabookstoreservice.cart.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.BookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.cart.domain.Cart;
import com.nhnacademy._vidiabookstoreservice.cart.domain.CartBook;
import com.nhnacademy._vidiabookstoreservice.cart.domain.CartOwner;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.AddCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.UpdateCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.BookSummaryResponse;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.CartBookResponse;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.CartResponse;
import com.nhnacademy._vidiabookstoreservice.cart.exception.CartBookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.cart.repository.jpa.CartRepository;
import com.nhnacademy._vidiabookstoreservice.cart.repository.redis.RedisCartRepository;
import com.nhnacademy._vidiabookstoreservice.cart.service.CartService;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 장바구니 기능 전체 제공 ( Redis + MySQL )
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final RedisCartRepository redisCartRepository;
    private final UserRepository userRepository;

    // ==== 조회 ==== //
    /**
     * 장바구니 조회 (모든 아이템)
     */
    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(CartOwner owner) {
        ensureLoaded(owner);

        Map<Long, Integer> redisItems = redisCartRepository.getCartItems(owner);

        List<CartBookResponse> items = redisItems.entrySet().stream()
                .map(entry -> toCartBookResponseOrCleanup(owner, entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .toList();

        redisCartRepository.refreshTtlIfDataKeyExists(owner);

        return new CartResponse(owner.isUser() ? owner.id() : null, items);
    }

    /**
     * 존재하지 않는 책이 포함되어 있으면 제거 후 조회
     */
    private CartBookResponse toCartBookResponseOrCleanup(CartOwner owner, Long bookId, int quantity) {
        return bookRepository.findById(bookId)
                .map(book -> CartBookResponse.of(BookSummaryResponse.from(book), quantity))
                .orElseGet(() -> {
                    redisCartRepository.removeNoTtl(owner, bookId);
                    if (owner.isUser()) { // 회원이면 db에서도 지워주기
                        removeFromDbCart(owner.id(), bookId);
                    }
                    return null;
                });
    }

    /* ==== 쓰기 ==== */
    /**
     * 장바구니 도서 추가 (Redis에만 추가)
     * @param owner
     * @param addItemRequest : (bookId, quantity)
     */
    @Override
    public void addItem(CartOwner owner, AddCartItemRequest addItemRequest) {
        Long bookId = addItemRequest.bookId();
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }

        ensureLoaded(owner);
        redisCartRepository.incrementItemQuantity(owner, bookId, addItemRequest.quantity());
    }

    /**
     * 장바구니 도서 수량 수정 (Redis만 수정)
     * @param owner
     * @param bookId
     * @param updateRequest : quantity
     */
    @Override
    public void updateItem(CartOwner owner, Long bookId, UpdateCartItemRequest updateRequest) {
        ensureLoaded(owner);

        Map<Long, Integer> items = redisCartRepository.getCartItems(owner);
        if (!items.containsKey(bookId)) {
            throw new CartBookNotFoundException(owner.id(), bookId);
        }

        redisCartRepository.setItemQuantity(owner, bookId, updateRequest.quantity());
    }

    /**
     * 장바구니 도서 삭제
     * 1. redis에서만 삭제
     * 2. 회원인데, redis가 비어있으면 db에서도 삭제
     * @param owner
     * @param bookId
     */
    @Override
    public void removeItem(CartOwner owner, Long bookId) {
        ensureLoaded(owner);
        redisCartRepository.removeItem(owner, bookId);

        if (owner.isUser() && redisCartRepository.isEmpty(owner)) {
            cartRepository.findByUserId(owner.id())
                    .ifPresent(cart -> cart.getCartBooks().clear());
        }
    }

    /**
     * 주문한 도서 삭제 -> removeItem() 호출
     * @param userId
     * @param orderBooks
     */
    @Override
    public void removeItemByOrder(Long userId, List<Long> orderBooks) {
        CartOwner owner = userRepository.existsById(userId) ? CartOwner.user(userId) : CartOwner.guest(userId);
        orderBooks.forEach(bookId -> removeItem(owner, bookId));
    }

    /**
     * 장바구니 비우기 ( 비회원/회원 머지 후 비회원 장바구니 비우기 )
     * @param owner
     */
    @Override
    public void clear(CartOwner owner) {
        redisCartRepository.clearCart(owner);
    }

    /**
     *  장바구니 삭제 ( 회원탈퇴 후 호출)
     * @param userId
     */
    @Override
    public void deleteCart(Long userId) {
        CartOwner owner = CartOwner.user(userId);
        redisCartRepository.clearCart(owner);

        cartRepository.findByUserId(userId).ifPresent(cartRepository::delete);
    }


    /**
     * 비회원 장바구니 아이템 수량 ( 팝업에 띄울려고 )
     * @param guestId
     * @return
     */
    @Override
    public int countGuestCartItems(Long guestId) {
        return redisCartRepository.getCartItems(CartOwner.guest(guestId)).size();
    }

    /* ==== 로그인/로그아웃/머지 ==== */

    /**
     * 로그인 시 mysql -> redis
     * 만약 redis에 데이터가 있으면 return
     * @param userId
     */
    @Override
    @Transactional(readOnly = true)
    public void loginSyncCart(Long userId) {
        CartOwner owner = CartOwner.user(userId);

        // 로그아웃 하지 않은 회원 + 스케줄러 처리 안됐을때
        if (!redisCartRepository.getCartItems(owner).isEmpty()) {
            return;
        }

        Map<Long, Integer> restored = buildRestoreMapAndCleanupDb(userId);

        if (!restored.isEmpty()) {
            redisCartRepository.putAllNoTtl(owner, restored);
            redisCartRepository.refreshTtlIfDataKeyExists(owner);
        }
    }

    /**
     * 로그아웃 시 redis -> mysql
     * @param userId
     */
    @Override
    public void logoutSyncCart(Long userId) {
        CartOwner owner = CartOwner.user(userId);

        if (redisCartRepository.getCartItems(owner).isEmpty()) {
            redisCartRepository.clearCart(owner);
            return;
        }

        performFlush(userId);
        redisCartRepository.clearCart(owner);
    }

    /**
     * 비회원 장바구니, 회원 장바구니 머지
     * @param guestId
     * @param userId
     */
    @Override
    public void mergeGuestCartToUser(Long guestId, Long userId) {
        CartOwner guest = CartOwner.guest(guestId);
        CartOwner user = CartOwner.user(userId);

        Map<Long, Integer> guestItems = redisCartRepository.getCartItems(guest);
        if (guestItems.isEmpty()) {
            redisCartRepository.clearCart(guest);
            return;
        }

        guestItems.forEach((bookId, quantity) -> redisCartRepository.setItemQuantity(user, bookId, quantity));
        redisCartRepository.clearCart(guest);
    }

    /* ==== 스케줄러 flush ==== */

    /**
     * 장바구니 동기화 스케줄러가 호출 ( DB 최신화 시킨 후 redis에서 삭제 )
     * @param userId
     */
    @Override
    public void flushCartFromRedisToMySql(Long userId) {
        if (redisCartRepository.existsExpireKey(userId)) {
            log.info("[장바구니] 유저(userId={})가 다시 활동 중이므로 flush를 중단", userId);
            return;
        }
        performFlush(userId);
        redisCartRepository.clearCart(CartOwner.user(userId));
    }

    /**
     *  Redis -> DB 반영
     * - Redis empty면 DB를 비우지 않는다 (TTL/캐시 손실 방지)
     */
    private void performFlush(Long userId) {
        CartOwner owner = CartOwner.user(userId);
        Map<Long, Integer> items = redisCartRepository.getCartItems(owner);

        if (items.isEmpty()) {
            log.warn("[장바구니] 레디스가 비어있음 userId={}", userId);
            return;
        }

        Cart cart = findOrCreateCart(userId);

        List<Long> bookIds = new ArrayList<>(items.keySet());
        Map<Long, Book> bookMap = bookRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, b -> b));

        Map<Long, CartBook> existingMap = cart.getCartBooks().stream()
                .collect(Collectors.toMap(cb -> cb.getBook().getId(), cb -> cb));

        items.forEach((bookId, qty) -> {
            CartBook existing = existingMap.remove(bookId);
            if (existing != null) {
                existing.changeQuantity(qty);
                return;
            }

            Book book = bookMap.get(bookId);
            if (book != null) {
                cart.addItem(book, qty);
            } else {
                // Redis에 잘못 남은 bookId 정리(TTL 갱신 X)
                redisCartRepository.removeNoTtl(owner, bookId);
            }
        });

        // Redis에 없는 기존 항목 제거
        existingMap.values().forEach(cb -> cart.getCartBooks().remove(cb));
    }

    /**
     *  Redis TTL로 cart key가 날아갔으면 DB에서 복구 (조회, 추가, 수정, 삭제 시 호출)
     */
    private void ensureLoaded(CartOwner owner) {
        if (!owner.isUser()) return;

        // 레디스에 이미 존재하면
        if (redisCartRepository.existsDataKey(owner)) {
            redisCartRepository.refreshTtlIfDataKeyExists(owner);
            return;
        }

        Map<Long, Integer> restored = buildRestoreMapAndCleanupDb(owner.id());
        if (!restored.isEmpty()) {
            redisCartRepository.putAllNoTtl(owner, restored);
        }
    }

    /**
     * DB cart에서 유효한 책만 restore map으로 만들고(삭제된 책은 DB에서 제거)
     */
    private Map<Long, Integer> buildRestoreMapAndCleanupDb(Long userId) {
        Map<Long, Integer> restored = new HashMap<>();

        try{
            cartRepository.findByUserId(userId).ifPresent(cart -> {
                cart.getCartBooks().removeIf(cb -> {
                    Long bookId = cb.getBook().getId();
                    if (!bookRepository.existsById(bookId)) return true;
                    restored.put(bookId, cb.getQuantity());
                    return false;
                });
            });
        }catch (Exception e){
            log.error("[장바구니 복구 실패] userId={}", userId, e);
        }

        return restored;
    }

    private Cart findOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));
    }

    private void removeFromDbCart(Long userId, Long bookId) {
        cartRepository.findByUserId(userId).ifPresent(cart ->
                cart.getCartBooks().removeIf(cb -> cb.getBook().getId().equals(bookId))
        );
    }
}