package com.nhnacademy._vidiabookstoreservice.cart.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.cart.domain.CartOwner;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.AddCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.UpdateCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.repository.jpa.CartRepository;
import com.nhnacademy._vidiabookstoreservice.cart.repository.redis.DirtyCartRepository;
import com.nhnacademy._vidiabookstoreservice.cart.repository.redis.RedisCartRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {
    @Mock
    private CartRepository cartRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private RedisCartRepository redisCartRepository;

    @Mock
    private DirtyCartRepository dirtyCartRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private CartOwner cartOwner;

    @BeforeEach
    void setup() {
        cartOwner = CartOwner.user(1L);
    }

    @Test
    @DisplayName("장바구니 아이템 추가 성공")
    void addItem_success(){
        AddCartItemRequest request = new AddCartItemRequest(1L,2);
        when(bookRepository.existsById(request.bookId())).thenReturn(true);

        cartService.addItem(cartOwner, request);

        verify(redisCartRepository, times(1))
                .incrementItemQuantity(cartOwner, request.bookId(), request.quantity());

        verify(dirtyCartRepository, times(1)).markDirty(cartOwner.id());
    }

    @Test
    @DisplayName("장바구니 아이템 추가 실패 - 도서 not found")
    void addItem_bookNotFound() {
        AddCartItemRequest request = new AddCartItemRequest(99L, 1);
        when(bookRepository.existsById(request.bookId())).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cartService.addItem(cartOwner, request));
        assertEquals("도서를 찾을 수 없습니다.", ex.getMessage());

        verify(redisCartRepository, never()).incrementItemQuantity(any(), anyLong(), anyInt());
        verify(dirtyCartRepository, never()).markDirty(anyLong());
    }

    @Test
    @DisplayName("장바구니 도서 조회 성공")
    void getCart_success() {
        Map<Long, Integer> redisItems = Map.of(1L, 2);
        when(redisCartRepository.getCartItems(cartOwner)).thenReturn(redisItems);
        Book book = Book.builder()
                .title("테스트 책")
                .priceStandard(10000)
                .priceSales(10000)
                .stock(10)
                .volumeNumber(1)
                .packagingAvailable(true)
                .build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        var response = cartService.getCart(cartOwner);

        assertNotNull(response);
        assertEquals(1, response.items().size());
        assertEquals(2, response.items().get(0).quantity());
    }

    @Test
    @DisplayName("장바구니 도서 수량 수정 성공")
    void updateItem_success() {
        Long bookId = 1L;
        when(bookRepository.existsById(bookId)).thenReturn(true);
        when(redisCartRepository.getCartItems(cartOwner)).thenReturn(Map.of(bookId, 1));

        cartService.updateItem(cartOwner, bookId, new UpdateCartItemRequest(5));

        verify(redisCartRepository, times(1)).setItemQuantity(cartOwner, bookId, 5);
        verify(dirtyCartRepository, times(1)).markDirty(cartOwner.id());
    }

    @Test
    @DisplayName("장바구니 수량 실패 - 없는 도서")
    void updateItem_bookNotInCart() {
        Long bookId = 1L;
        when(bookRepository.existsById(bookId)).thenReturn(true);
        when(redisCartRepository.getCartItems(cartOwner)).thenReturn(Map.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cartService.updateItem(cartOwner, bookId, new UpdateCartItemRequest(5)));
        assertEquals("장바구니에 담겨있지 않은 도서입니다.", ex.getMessage());
    }

}
