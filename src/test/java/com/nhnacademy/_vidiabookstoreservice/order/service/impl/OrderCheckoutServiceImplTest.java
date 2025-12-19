package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.exception.invalid.BookStockNotEnoughException;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.BookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.global.exception.NoSuchElementException;
import com.nhnacademy._vidiabookstoreservice.order.domain.CheckoutSession;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.BookOrderResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderCheckoutResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.PackagingOptionResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.PackagingOptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCheckoutServiceImplTest {

    @InjectMocks
    private OrderCheckoutServiceImpl orderCheckoutService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private PackagingOptionService packagingOptionService;

    @Mock
    private BookService bookService;

    @Mock
    private RedisTemplate<String, Object> orderRedisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;


    @Test
    @DisplayName("주문 시작(initCheckout) - 성공: 재고 충분, Redis 저장 확인")
    void initiateCheckout_Success() {
        Long bookId = 1L;
        int quantity = 2;
        int currentStock = 10;

        List<OrderCheckoutRequest> requests = List.of(new OrderCheckoutRequest(bookId, quantity));

        given(bookRepository.findIdsAndStocksById(anyList())).willReturn(List.<Object[]>of(new Object[]{bookId, currentStock}));

        given(orderRedisTemplate.opsForValue()).willReturn(valueOperations);

        String uuid = orderCheckoutService.initiateCheckout(requests);

        assertThat(uuid).isNotNull();
        verify(valueOperations).set(
                anyString(),
                any(CheckoutSession.class),
                eq(30L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("주문 시작 - 실패: 존재하지 않는 도서 ID")
    void initiateCheckout_Fail_BookNotFound() {
        Long bookId = 999L;
        List<OrderCheckoutRequest> requests = List.of(new OrderCheckoutRequest(bookId, 1));

        given(bookRepository.findIdsAndStocksById(anyList())).willReturn(Collections.emptyList());

        assertThatThrownBy(() -> orderCheckoutService.initiateCheckout(requests))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    @DisplayName("주문 시작 - 실패: 재고 부족")
    void initiateCheckout_Fail_StockNotEnough() {
        Long bookId = 1L;
        int requestQuantity = 10;
        int currentStock = 5;

        List<OrderCheckoutRequest> requests = List.of(new OrderCheckoutRequest(bookId, requestQuantity));

        given(bookRepository.findIdsAndStocksById(anyList())).willReturn(List.<Object[]>of(new Object[]{bookId, currentStock}));

        assertThatThrownBy(() -> orderCheckoutService.initiateCheckout(requests))
                .isInstanceOf(BookStockNotEnoughException.class);
    }

    @Test
    @DisplayName("주문 응답 조회 - 성공")
    void getOrderCheckoutResponse() {
        String key = "test-uuid";
        Long bookId1 = 1L;
        Long bookId2 = 2L;
        int quantity1 = 2;
        int quantity2 = 1;
        int price1 = 10000;
        int price2 = 20000;
        String kdc = "008";

        List<OrderCheckoutRequest> requests = List.of(
                new OrderCheckoutRequest(bookId1, quantity1),
                new OrderCheckoutRequest(bookId2, quantity2)
        );
        CheckoutSession session = new CheckoutSession(requests);

        given(orderRedisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("checkout:session:" + key)).willReturn(session);

        BookOrderResponse book1 = new BookOrderResponse(bookId1, "Java", "작가", "img1", price1, kdc);
        BookOrderResponse book2 = new BookOrderResponse(bookId2, "Spring", "작가2", "img2", price2, kdc);

        given(bookService.getOrderBookByBookIds(anyList())).willReturn(List.of(book1, book2));
        given(packagingOptionService.getPackagingOptions()).willReturn(List.of(new PackagingOptionResponse(1L, "포장지1", 1000)));

        OrderCheckoutResponse orderCheckoutResponse = orderCheckoutService.getOrderCheckoutResponse(1L, key);

        assertThat(orderCheckoutResponse).isNotNull();

        int expectAmount = (price1 * quantity1) + (price2 * quantity2);
        assertThat(orderCheckoutResponse.finalAmount()).isEqualTo(expectAmount);

        assertThat(orderCheckoutResponse.orderName()).contains("Java");
        assertThat(orderCheckoutResponse.orderName()).contains("외 2권");

        assertThat(orderCheckoutResponse.deliveryDateResponses()).hasSize(6);
    }

    @Test
    @DisplayName("주문 응답 조회 - 실패 : Redis 세션 만료")
    void getOrderCheckoutResponse_Fail_SessionExpired() {
        String key = "expired-uuid";

        given(orderRedisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(anyString())).willReturn(null);

        assertThatThrownBy(() -> orderCheckoutService.getOrderCheckoutResponse(1L, key))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("주문 세션이 만료되었거나 존재하지 않습니다.");
    }

    @Test
    @DisplayName("주문 응답 조회 - 실패 : 도서 정보 조회 실패")
    void getOrderCheckoutResponse_Fail_BookInfoMismatch() {
        String key = "test-uuid";

        List<OrderCheckoutRequest> requests = List.of(
                new OrderCheckoutRequest(1L, 1),
                new OrderCheckoutRequest(2L, 2)
        );

        CheckoutSession session = new CheckoutSession(requests);

        given(orderRedisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("checkout:session:" + key)).willReturn(session);

        BookOrderResponse book1 = new BookOrderResponse(1L, "Title", "author", "img", 10000, "008");
        given(bookService.getOrderBookByBookIds(anyList())).willReturn(List.of(book1));

        assertThatThrownBy(() -> orderCheckoutService.getOrderCheckoutResponse(1L, key))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("요청한 상품 중 일부 상품 정보를 찾을 수 없습니다.");
    }


}