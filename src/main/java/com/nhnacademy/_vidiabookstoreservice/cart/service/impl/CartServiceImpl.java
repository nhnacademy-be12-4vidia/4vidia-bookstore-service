package com.nhnacademy._vidiabookstoreservice.cart.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.cart.domain.Cart;
import com.nhnacademy._vidiabookstoreservice.cart.domain.CartBook;
import com.nhnacademy._vidiabookstoreservice.cart.domain.CartOwner;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.AddCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.UpdateCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.BookSummaryResponse;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.CartBookResponse;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.CartResponse;
import com.nhnacademy._vidiabookstoreservice.cart.repository.jpa.CartBookRepository;
import com.nhnacademy._vidiabookstoreservice.cart.repository.jpa.CartRepository;
import com.nhnacademy._vidiabookstoreservice.cart.repository.redis.DirtyCartRepository;
import com.nhnacademy._vidiabookstoreservice.cart.repository.redis.RedisCartRepository;
import com.nhnacademy._vidiabookstoreservice.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 장바구니 기능 전체 제공 ( Redis + MySQL )
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final RedisCartRepository redisCartRepository;
    private final DirtyCartRepository dirtyCartRepository;
    private final CartBookRepository cartBookRepository;

    /**
     * 장바구니 조회 (모든 아이템)
     * @param owner : CartOwner
     * @return CartResponse (List<CartItemResponse>, totalPrice)
     *                      CartItemResponse : bookId, title, price, quantity, lineTotal
     * Redis에서 조회
     */
    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(CartOwner owner){
        Map<Long, Integer> redisItems = redisCartRepository.getCartItems(owner);

        List<CartBookResponse> items = redisItems.entrySet().stream()
                .map(entry -> {
                    Long bookId = entry.getKey();
                    int quantity = entry.getValue();

                    Book book = bookRepository.findById(bookId)
                            .orElseThrow(() -> new IllegalArgumentException("도서를 찾을 수 없습니다."));

                    BookSummaryResponse bookDto = BookSummaryResponse.from(book);
                    return CartBookResponse.of(bookDto, quantity);
                })
                .toList();

        if(owner.isUser()){
            return new CartResponse(Long.valueOf(owner.id()),items);
        }
        return new CartResponse(null,items);
    }


    /**
     * 장바구니 도서 추가
     * Redis만 추가
     * @param owner
     * @param addItemRequest : (bookId, quantity)
     */
    @Override
    public void addItem(CartOwner owner, AddCartItemRequest addItemRequest) {
        if(!bookRepository.existsById(addItemRequest.bookId())){
            throw new IllegalArgumentException("도서를 찾을 수 없습니다.");
        }

        // Redis 기준 수량 증가
       redisCartRepository.incrementItemQuantity(owner,
                addItemRequest.bookId(),
                addItemRequest.quantity());

        if(owner.isUser()){
            dirtyCartRepository.markDirty(Long.valueOf(owner.id()));
        }
    }

    /**
     * 장바구니 도서 수량 수정
     * Redis만 수정
     * @param owner
     * @param updateRequest : (bookId, quantity)
     */
    @Override
    public void updateItem(CartOwner owner, Long bookId, UpdateCartItemRequest updateRequest) {
        if(!bookRepository.existsById(bookId)){
            throw new IllegalArgumentException("도서를 찾을 수 없습니다.");
        }

        Map<Long, Integer> items = redisCartRepository.getCartItems(owner);
        if (!items.containsKey(bookId)) {
            throw new IllegalArgumentException("장바구니에 담겨있지 않은 도서입니다.");
        }

        // Redis
        redisCartRepository.setItemQuantity(owner, bookId, updateRequest.quantity());
        if (owner.isUser()) {
            dirtyCartRepository.markDirty(Long.valueOf(owner.id()));
        }
    }

    /**
     * 장바구니 도서 삭제
     * Redis만 삭제
     * @param owner
     * @param bookId;
     */
    @Override
    public void removeItem(CartOwner owner, Long bookId){
        //TODO 유저아이디로 회원 조회
        // 회원이면 더티유저 등록, 카트에 해당아이템 삭제, 아니면 카트에 해당아이템만 삭제
        if(!bookRepository.existsById(bookId)){
            throw new IllegalArgumentException("도서를 찾을 수 없습니다.");
        }

        redisCartRepository.removeItem(owner,bookId);
        if (owner.isUser()) {
            dirtyCartRepository.markDirty(Long.valueOf(owner.id()));
        }
    }

    /**
     * 장바구니 비우기
     * @param owner;
     */
    @Override
    public void clear(CartOwner owner) {
        redisCartRepository.clearCart(owner);
        if (owner.isUser()) {
            dirtyCartRepository.markDirty(Long.valueOf(owner.id()));
        }
    }

    /**
     * 장바구니 삭제 : 회원에게 직접 제공 x, 회원 탈퇴 시 처리
     * Redis + MySQL 둘 다 삭제 (dirty에서도)
     * @param userId;
     */
    @Override
    public void deleteCart(Long userId){
        CartOwner owner = CartOwner.user(userId);
        redisCartRepository.clearCart(owner);
        dirtyCartRepository.remove(userId);

        if(cartRepository.existsByUserId(userId)){
            Cart cart = findCart(userId);
            cartRepository.delete(cart);
        }
    }

    @Override
    public int countGuestCartItems(String guestId) {
        CartOwner guest = CartOwner.guest(guestId);
        Map<Long, Integer> items = redisCartRepository.getCartItems(guest);

        return items.size();
    }


    /* =============== user 장바구니 찾기 (MySQL) =============== */

    /**
      * 장바구니 생성 메서드
      * @param userId (회원 : userId- > 카트 조회 -> 없으면 생성)
     * @return Cart
      */
    @Transactional(readOnly = true)
    protected Cart findCart(Long userId){ // 장바구니 없으면 에러
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("userId: " + userId + "의 장바구니가 존재하지 않습니다."));
    }

    private Cart findOrCreateCart(Long userId){ // 장바구니 없으면 생성
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));
    }

    @Override
    public void mergeGuestCartToUser(String guestId, Long userId){
        CartOwner guest = CartOwner.guest(guestId);
        CartOwner user = CartOwner.user(userId);


        Map<Long, Integer> guestItems = redisCartRepository.getCartItems(guest);
        if(guestItems.isEmpty()){
            redisCartRepository.clearCart(guest);
            return;
        }

        // 회원 Redis 장바구니에 합치기
        guestItems.forEach((bookId, quantity) -> {
            redisCartRepository.setItemQuantity(user, bookId, quantity);
        });

        dirtyCartRepository.markDirty(userId); // 변경 기록

        // guest redis 삭제
        redisCartRepository.clearCart(guest);
    }


    /**
     * Redis -> MySQL
     * @param userId
     */
    @Override
    public void flushCartFromRedisToMySql(Long userId) {
        CartOwner owner = CartOwner.user(userId);

        Map<Long, Integer> items = redisCartRepository.getCartItems(owner);

        Cart cart = findOrCreateCart(userId);

        // Redis 장바구니가 비어 있으면 -> MySQL (cart_book) 비우기
        if (items.isEmpty()) {
            cart.getCartBooks().clear();
            return;
        }

        // ! 현재 Cart의 CartBook들을 bookId 기준으로 맵핑
        Map<Long, CartBook> existingMap = cart.getCartBooks().stream()
                .collect(Collectors.toMap(
                        cb -> cb.getBook().getId(),
                        cb -> cb
                ));

        // Redis 기준으로 insert/update
        items.forEach((bookId, quantity) -> {
            CartBook existing = existingMap.remove(bookId);

            if (existing != null) {
                existing.changeQuantity(quantity); // 이미 있으면 수량 합
            } else {
                Book book = bookRepository.findById(bookId)
                        .orElseThrow(() ->
                                new IllegalStateException("도서를 찾을 수 없습니다. bookId=" + bookId)
                        );
                cart.addItem(book, quantity);
            }
        });

        // DB에는 있는데 Redis에는 없는 도서 => 삭제
        existingMap.values().forEach(cb -> cart.getCartBooks().remove(cb));
    }

    @Override
    public void logoutCart(Long userId) {
        CartOwner owner = CartOwner.user(userId);

        flushCartFromRedisToMySql(userId);
        redisCartRepository.clearCart(owner);
        dirtyCartRepository.remove(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public void loginSyncCart(Long userId) {
        CartOwner owner = CartOwner.user(userId);

        cartRepository.findByUserId(userId).ifPresent(cart -> {
            redisCartRepository.clearCart(owner);

            cart.getCartBooks().forEach(cartBook -> {
                Long bookId = cartBook.getBook().getId();
                int quantity = cartBook.getQuantity();

                redisCartRepository.setItemQuantity(owner, bookId, quantity);
            });
        });
    }


}
