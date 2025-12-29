package com.nhnacademy._vidiabookstoreservice.cart.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.BookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.cart.domain.Cart;
import com.nhnacademy._vidiabookstoreservice.cart.domain.CartBook;
import com.nhnacademy._vidiabookstoreservice.cart.domain.CartOwner;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.AddCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.UpdateCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.CartResponse;
import com.nhnacademy._vidiabookstoreservice.cart.exception.CartBookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.cart.repository.jpa.CartRepository;
import com.nhnacademy._vidiabookstoreservice.cart.repository.redis.RedisCartRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock private CartRepository cartRepository;
    @Mock private BookRepository bookRepository;
    @Mock private RedisCartRepository redisCartRepository;
    @Mock private UserRepository userRepository;

    private final Long userId = 1L;
    private final Long bookId = 100L;
    private CartOwner userOwner;

    @BeforeEach
    void setUp() {
        userOwner = CartOwner.user(userId);
    }

    @Test
    @DisplayName("getCart: Redis에 데이터가 없으면 DB에서 로드하여 반환")
    void getCart_RestoreFromDb_WhenRedisEmpty() {
        // given
        when(redisCartRepository.existsDataKey(userOwner)).thenReturn(false);
        Cart cart = new Cart(userId);
        Book book = mock(Book.class);
        when(book.getId()).thenReturn(bookId);
        cart.addItem(book, 2);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(bookRepository.existsById(bookId)).thenReturn(true);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(redisCartRepository.getCartItems(userOwner)).thenReturn(Map.of(bookId, 2));

        // when
        CartResponse response = cartService.getCart(userOwner);

        // then
        assertThat(response.items()).hasSize(1);
        verify(redisCartRepository).putAllNoTtl(eq(userOwner), anyMap());
    }

    @Test
    @DisplayName("getCart: 도서가 DB에 존재하지 않으면 Redis와 DB에서 해당 도서를 삭제한다")
    void getCart_Cleanup_WhenBookNotFound() {
        // given
        when(redisCartRepository.existsDataKey(userOwner)).thenReturn(true);
        when(redisCartRepository.getCartItems(userOwner)).thenReturn(Map.of(bookId, 2));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // when
        cartService.getCart(userOwner);

        // then
        verify(redisCartRepository).removeNoTtl(userOwner, bookId);
        verify(cartRepository).findByUserId(userId);
    }

    // ==== addItem 테스트 ====

    @Test
    @DisplayName("addItem: 새로운 도서 추가 성공")
    void addItem_Success() {
        // given
        AddCartItemRequest request = new AddCartItemRequest(bookId, 1);
        when(bookRepository.existsById(bookId)).thenReturn(true);
        when(redisCartRepository.existsDataKey(userOwner)).thenReturn(true);

        // when
        cartService.addItem(userOwner, request);

        // then
        verify(redisCartRepository).incrementItemQuantity(userOwner, bookId, 1);
    }

    @Test
    @DisplayName("addItem: 존재하지 않는 도서 추가 시 BookNotFoundException 발생")
    void addItem_Fail_BookNotFound() {
        // given
        AddCartItemRequest request = new AddCartItemRequest(bookId, 1);
        when(bookRepository.existsById(bookId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> cartService.addItem(userOwner, request))
                .isInstanceOf(BookNotFoundException.class);
    }

    // ==== updateItem 테스트 ====

    @Test
    @DisplayName("updateItem: 도서 수량 수정 성공")
    void updateItem_Success() {
        // given
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);
        when(redisCartRepository.existsDataKey(userOwner)).thenReturn(true);
        when(redisCartRepository.getCartItems(userOwner)).thenReturn(Map.of(bookId, 1));

        // when
        cartService.updateItem(userOwner, bookId, request);

        // then
        verify(redisCartRepository).setItemQuantity(userOwner, bookId, 5);
    }

    @Test
    @DisplayName("updateItem: 장바구니에 없는 도서 수정 시 CartBookNotFoundException 발생")
    void updateItem_Fail_NotFoundInCart() {
        // given
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);
        when(redisCartRepository.existsDataKey(userOwner)).thenReturn(true);
        when(redisCartRepository.getCartItems(userOwner)).thenReturn(Collections.emptyMap());

        // when & then
        assertThatThrownBy(() -> cartService.updateItem(userOwner, bookId, request))
                .isInstanceOf(CartBookNotFoundException.class);
    }

    // ==== removeItem 테스트 ====

    @Test
    @DisplayName("removeItem: 아이템 삭제 후 Redis가 비면 DB CartBook도 비운다")
    void removeItem_AndClearDb_WhenEmpty() {
        // given
        when(redisCartRepository.existsDataKey(userOwner)).thenReturn(true);
        when(redisCartRepository.isEmpty(userOwner)).thenReturn(true);
        Cart cart = new Cart(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // when
        cartService.removeItem(userOwner, bookId);

        // then
        verify(redisCartRepository).removeItem(userOwner, bookId);
        assertThat(cart.getCartBooks()).isEmpty();
    }

    // ==== login/logout/merge Sync 테스트 ====

    @Test
    @DisplayName("loginSyncCart: Redis에 데이터가 없을 때만 DB에서 복구")
    void loginSyncCart_Success() {
        // given
        when(redisCartRepository.existsDataKey(userOwner)).thenReturn(false);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when
        cartService.loginSyncCart(userId);

        // then
        verify(redisCartRepository).refreshTtlIfDataKeyExists(userOwner);
    }

    @Test
    @DisplayName("logoutSyncCart: 로그아웃 시 Flush 후 Redis 삭제")
    void logoutSyncCart_Success() {
        // given
        when(redisCartRepository.getCartItems(userOwner)).thenReturn(Map.of(bookId, 3));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(new Cart(userId)));

        // when
        cartService.logoutSyncCart(userId);

        // then
        verify(redisCartRepository).clearCart(userOwner);
    }

    @Test
    @DisplayName("mergeGuestCartToUser: 비회원 장바구니 데이터를 회원 Redis로 병합")
    void mergeGuestCartToUser_Success() {
        // given
        Long guestId = 12345L;
        Map<Long, Integer> guestItems = Map.of(bookId, 2);
        when(redisCartRepository.getCartItems(CartOwner.guest(guestId))).thenReturn(guestItems);

        // when
        cartService.mergeGuestCartToUser(guestId, userId);

        // then
        verify(redisCartRepository).putAllNoTtl(eq(userOwner), eq(guestItems));
        verify(redisCartRepository).clearCart(CartOwner.guest(guestId));
    }

    // ==== Scheduler Flush 테스트 ====

    @Test
    @DisplayName("flushCartFromRedisToMySql: 유저가 활동 중이면 작업을 중단한다")
    void flush_Abort_WhenUserActive() {
        // given
        when(redisCartRepository.existsExpireKey(userId)).thenReturn(true);

        // when
        cartService.flushCartFromRedisToMySql(userId);

        // then
        verify(cartRepository, never()).findByUserId(anyLong());
    }

    @Test
    @DisplayName("flushCartFromRedisToMySql: Redis 데이터를 DB에 반영하고 Redis를 비운다")
    void flush_Success_WhenUserInactive() {
        // given
        when(redisCartRepository.existsExpireKey(userId)).thenReturn(false);
        when(redisCartRepository.getCartItems(userOwner)).thenReturn(Map.of(bookId, 10));
        Cart cart = new Cart(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        Book book = mock(Book.class);
        when(book.getId()).thenReturn(bookId);
        when(bookRepository.findAllById(anySet())).thenReturn(List.of(book));

        // when
        cartService.flushCartFromRedisToMySql(userId);

        // then
        assertThat(cart.getCartBooks()).hasSize(1);
        assertThat(cart.getCartBooks().get(0).getQuantity()).isEqualTo(10);
        verify(redisCartRepository).clearCart(userOwner);
    }

    @Test
    @DisplayName("deleteCart: Redis와 DB에서 장바구니 정보 삭제")
    void deleteCart_Success() {
        // given
        Cart cart = new Cart(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // when
        cartService.deleteCart(userId);

        // then
        verify(redisCartRepository).clearCart(userOwner);
        verify(cartRepository).delete(cart);
    }

    @Test
    @DisplayName("performFlush 상세: 1.수량변경 2.새항목추가 3.DB에만있는항목삭제 4.존재하지않는도서삭제가 동시에 일어날 때")
    void performFlush_ComplexScenario() {
        // given
        CartOwner owner = CartOwner.user(userId);

        // Redis 상황: 도서A(수량변경), 도서B(새로추가), 도서C(실제 존재하지 않는 도서)
        Map<Long, Integer> redisItems = Map.of(
                101L, 5,  // 수량 변경 대상
                102L, 2,  // 새 항목 추가 대상
                999L, 1   // DB에 없는 가짜 도서
        );
        when(redisCartRepository.getCartItems(owner)).thenReturn(redisItems);

        // DB 상황: 도서A(기존), 도서D(Redis에 없으므로 삭제 대상)
        Cart cart = new Cart(userId);
        Book bookA = mock(Book.class); when(bookA.getId()).thenReturn(101L);
        Book bookD = mock(Book.class); when(bookD.getId()).thenReturn(104L);
        cart.addItem(bookA, 1);
        cart.addItem(bookD, 1);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // 도서 조회 상황: A와 B는 존재, 999번은 존재하지 않음
        Book bookB = mock(Book.class); when(bookB.getId()).thenReturn(102L);
        when(bookRepository.findAllById(anySet())).thenReturn(List.of(bookA, bookB));

        // when
        cartService.flushCartFromRedisToMySql(userId);

        // then
        // 1. 수량 변경 확인 (도서A: 1 -> 5)
        CartBook cbA = cart.getCartBooks().stream().filter(cb -> cb.getBook().getId().equals(101L)).findFirst().get();
        assertThat(cbA.getQuantity()).isEqualTo(5);

        // 2. 새 항목 추가 확인 (도서B)
        boolean hasBookB = cart.getCartBooks().stream().anyMatch(cb -> cb.getBook().getId().equals(102L));
        assertThat(hasBookB).isTrue();

        // 3. Redis에 없는 항목 DB에서 삭제 확인 (도서D 제거됨)
        assertThat(cart.getCartBooks()).hasSize(2); // A와 B만 남아야 함

        // 4. 존재하지 않는 도서(999) Redis 정리 확인
        verify(redisCartRepository).removeNoTtl(owner, 999L);
    }

    @Test
    @DisplayName("getCart: 회원이 도서를 조회할 때 DB에는 있지만 Redis에는 없는 도서가 삭제된 도서면 DB에서도 즉시 지운다")
    void getCart_SyncCleanupWithDb() {
        // given
        CartOwner owner = CartOwner.user(userId);
        Long deletedBookId = 500L;

        when(redisCartRepository.existsDataKey(owner)).thenReturn(true);
        when(redisCartRepository.getCartItems(owner)).thenReturn(Map.of(deletedBookId, 1));

        // 도서가 삭제됨
        when(bookRepository.findById(deletedBookId)).thenReturn(Optional.empty());

        // DB 카트 상황
        Cart cart = new Cart(userId);
        Book mockBook = mock(Book.class); when(mockBook.getId()).thenReturn(deletedBookId);
        cart.addItem(mockBook, 1);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // when
        cartService.getCart(owner);

        // then
        verify(redisCartRepository).removeNoTtl(owner, deletedBookId);
        // DB에서도 지워졌는지 확인
        assertThat(cart.getCartBooks()).isEmpty();
    }

    @Test
    @DisplayName("removeItemByOrder: 여러 권 주문 시 주문한 책들만 정확히 삭제된다")
    void removeItemByOrder_MultipleBooks() {
        // given
        List<Long> orderBookIds = List.of(101L, 102L);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(redisCartRepository.existsDataKey(any())).thenReturn(true);

        // when
        cartService.removeItemByOrder(userId, orderBookIds);

        // then
        verify(redisCartRepository, times(1)).removeItem(any(), eq(101L));
        verify(redisCartRepository, times(1)).removeItem(any(), eq(102L));
    }

    @Test
    @DisplayName("restoreFromDb: DB 복구 시점에 이미 삭제된 도서가 DB에 남아있다면 걸러내고 Redis에 넣는다")
    void restoreFromDb_FilteringDeletedBooks() {
        // given
        Cart cart = new Cart(userId);
        Book normalBook = mock(Book.class); when(normalBook.getId()).thenReturn(1L);
        Book deletedBook = mock(Book.class); when(deletedBook.getId()).thenReturn(2L);

        cart.addItem(normalBook, 2);
        cart.addItem(deletedBook, 5);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(bookRepository.existsById(1L)).thenReturn(true);  // 정상 도서
        when(bookRepository.existsById(2L)).thenReturn(false); // 삭제된 도서

        // when (private 메서드인 restoreFromDb를 loginSyncCart를 통해 호출)
        cartService.loginSyncCart(userId);

        // then
        // 정상 도서만 Redis에 복구되어야 함
        verify(redisCartRepository).putAllNoTtl(any(), eq(Map.of(1L, 2)));
        // DB에서도 삭제된 도서는 제거되어야 함
        assertThat(cart.getCartBooks()).hasSize(1);
    }

}