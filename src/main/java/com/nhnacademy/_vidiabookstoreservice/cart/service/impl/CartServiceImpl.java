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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final RedisCartRepository redisCartRepository;
    private final UserRepository userRepository;

    /**
     * 장바구니 조회 (모든 아이템)
     */
    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(CartOwner owner) {
        if (owner.isUser() && !redisCartRepository.existsDataKey(owner)) {
            restoreFromDb(owner.id());
        }

        Map<Long, Integer> redisItems = redisCartRepository.getCartItems(owner);

        List<CartBookResponse> items = redisItems.entrySet().stream()
                .map(e -> toCartBookResponseOrCleanup(owner, e.getKey(), e.getValue()))
                .filter(Objects::nonNull)
                .toList();

        redisCartRepository.refreshTtlIfDataKeyExists(owner);

        return new CartResponse(owner.isUser() ? owner.id() : null, items);
    }

    private CartBookResponse toCartBookResponseOrCleanup(CartOwner owner, Long bookId, int quantity) {
        return bookRepository.findById(bookId)
                .map(book -> CartBookResponse.of(BookSummaryResponse.from(book), quantity))
                .orElseGet(() -> {
                    redisCartRepository.removeNoTtl(owner, bookId);
                    if (owner.isUser()) {
                        removeFromDbCart(owner.id(), bookId);
                    }
                    return null;
                });
    }

    @Override
    public void addItem(CartOwner owner, AddCartItemRequest request) {
        Long bookId = request.bookId();
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }

        ensureRedisLoadedForWrite(owner);
        redisCartRepository.incrementItemQuantity(owner, bookId, request.quantity());
    }

    @Override
    public void updateItem(CartOwner owner, Long bookId, UpdateCartItemRequest request) {
        ensureRedisLoadedForWrite(owner);

        Map<Long, Integer> items = redisCartRepository.getCartItems(owner);
        if (!items.containsKey(bookId)) {
            throw new CartBookNotFoundException(owner.id(), bookId);
        }

        redisCartRepository.setItemQuantity(owner, bookId, request.quantity());
    }

    @Override
    public void removeItem(CartOwner owner, Long bookId) {
        ensureRedisLoadedForWrite(owner);
        redisCartRepository.removeItem(owner, bookId);

        if (owner.isUser() && redisCartRepository.isEmpty(owner)) {
            cartRepository.findByUserId(owner.id())
                    .ifPresent(cart -> cart.getCartBooks().clear());
        }
    }

    @Override
    public void removeItemByOrder(Long userId, List<Long> orderBooks) {
        CartOwner owner = userRepository.existsById(userId)
                ? CartOwner.user(userId)
                : CartOwner.guest(userId);

        orderBooks.forEach(bookId -> removeItem(owner, bookId));
    }

    @Override
    public void clear(CartOwner owner) {
        redisCartRepository.clearCart(owner);
    }

    @Override
    public void deleteCart(Long userId) {
        CartOwner owner = CartOwner.user(userId);
        redisCartRepository.clearCart(owner);
        cartRepository.findByUserId(userId).ifPresent(cartRepository::delete);
    }

    @Override
    public int countGuestCartItems(Long guestId) {
        return redisCartRepository.getCartItems(CartOwner.guest(guestId)).size();
    }

    /**
     * 로그인 직후 장바구니 화면 진입 전에 호출되는 optional preload 용도
     */
    @Override
    @Transactional(readOnly = true)
    public void loginSyncCart(Long userId) {
        CartOwner owner = CartOwner.user(userId);
        if (redisCartRepository.existsDataKey(owner)) return;

        restoreFromDb(userId);
        redisCartRepository.refreshTtlIfDataKeyExists(owner);
    }

    @Override
    public void logoutSyncCart(Long userId) {
        performFlush(userId);
        redisCartRepository.clearCart(CartOwner.user(userId));
    }

    @Override
    public void mergeGuestCartToUser(Long guestId, Long userId) {
        CartOwner guest = CartOwner.guest(guestId);
        CartOwner user = CartOwner.user(userId);

        Map<Long, Integer> guestItems = redisCartRepository.getCartItems(guest);
        if (guestItems.isEmpty()) {
            redisCartRepository.clearCart(guest);
            return;
        }

        redisCartRepository.putAllNoTtl(user, guestItems);
        redisCartRepository.refreshTtlIfDataKeyExists(user);
        redisCartRepository.clearCart(guest);
    }

    // Redis -> DB
    @Override
    public void flushCartFromRedisToMySql(Long userId) {
        if (redisCartRepository.existsExpireKey(userId)) {
            log.info("[장바구니] userId={} 활동 중 → flush 중단", userId);
            return;
        }
        performFlush(userId);
        redisCartRepository.clearCart(CartOwner.user(userId));
    }

    private void performFlush(Long userId) {
        CartOwner owner = CartOwner.user(userId);
        Map<Long, Integer> items = redisCartRepository.getCartItems(owner);
        if (items.isEmpty()) return;

        Cart cart = findOrCreateCart(userId);

        Map<Long, Book> bookMap = bookRepository.findAllById(items.keySet())
                .stream().collect(Collectors.toMap(Book::getId, b -> b));

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
                redisCartRepository.removeNoTtl(owner, bookId);
            }
        });

        existingMap.values().forEach(cb -> cart.getCartBooks().remove(cb));
    }

    private void ensureRedisLoadedForWrite(CartOwner owner) {
        if (!owner.isUser()) return;
        if (redisCartRepository.existsDataKey(owner)) return;
        restoreFromDb(owner.id());
    }

    private void restoreFromDb(Long userId) {
        Map<Long, Integer> restored = buildRestoreMapAndCleanupDb(userId);
        if (!restored.isEmpty()) {
            redisCartRepository.putAllNoTtl(CartOwner.user(userId), restored);
        }
    }

    // DB -> Redis
    private Map<Long, Integer> buildRestoreMapAndCleanupDb(Long userId) {
        Map<Long, Integer> restored = new HashMap<>();

        cartRepository.findByUserId(userId).ifPresent(cart ->
                cart.getCartBooks().removeIf(cb -> {
                    Long bookId = cb.getBook().getId();
                    if (!bookRepository.existsById(bookId)) return true;
                    restored.put(bookId, cb.getQuantity());
                    return false;
                })
        );
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