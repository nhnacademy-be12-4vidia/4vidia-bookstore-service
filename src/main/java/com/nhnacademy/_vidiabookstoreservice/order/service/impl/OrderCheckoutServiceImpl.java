package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.BookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.exception.invalid.BookStockNotEnoughException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.order.domain.CheckoutSession;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.*;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderIllegalArgumentException;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.OrderBookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.OrderRedisNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderCheckoutService;
import com.nhnacademy._vidiabookstoreservice.order.service.PackagingOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCheckoutServiceImpl implements OrderCheckoutService {
    private final BookRepository bookRepository;
    private final PackagingOptionService packagingOptionService;
    private final BookService bookService;

    private final RedisTemplate<String, Object> orderRedisTemplate;

    private static final String SESSION_PREFIX = "checkout:session:";


    /**
     * 재고 조회, 수량 체크후 레디스에 저장
     * @param orderCheckoutRequests : 주문 하는 도서 리스트 (아이디, 수량)
     * @return UUID 값 반환
     */
    @Override
    public String initiateCheckout(List<OrderCheckoutRequest> orderCheckoutRequests) {

        List<Long> bookIds = orderCheckoutRequests.stream()
                .map(OrderCheckoutRequest::bookId)
                .toList();

        List<Object[]> objects = bookRepository.findIdsAndStocksById(bookIds);

        Map<Long, Integer> stockMap = objects.stream()
                .collect(Collectors.toMap(
                        row -> (long) row[0],
                        row -> (int) row[1]
                ));

        for (OrderCheckoutRequest request : orderCheckoutRequests) {
            if (!stockMap.containsKey(request.bookId())) {
                throw new BookNotFoundException(request.bookId());
            }

            if (stockMap.get(request.bookId()) < request.quantity()) {
                throw new BookStockNotEnoughException();
            }
        }

        CheckoutSession checkoutSession = new CheckoutSession(orderCheckoutRequests);

        String uuid = UUID.randomUUID().toString();

        orderRedisTemplate.opsForValue().set(SESSION_PREFIX + uuid, checkoutSession, 30, TimeUnit.MINUTES);

        return uuid;
    }


    /**
     * 주문 화면에 보낼 값
     * @param userId : 회원이면 아이디, 비회원이면 null
     * @param key : 선택된 아이템을 꺼낼 레디스 키값
     * @return : 주문 화면에 보여줄 데이터
     */
    @Override
    @Transactional(readOnly = true) // 프론트의 자유도를 위해 묶지 말고 프론트에서 각각 호출하는 것도 방법
    public OrderCheckoutResponse getOrderCheckoutResponse(Long userId, String key) {

        CheckoutSession checkoutSession = (CheckoutSession) orderRedisTemplate.opsForValue().get(SESSION_PREFIX + key);
        if (checkoutSession == null) {
            throw new OrderRedisNotFoundException();
        }
        if (checkoutSession.orderCheckoutList.isEmpty()) {
            throw new OrderIllegalArgumentException();
        }
        List<OrderCheckoutRequest> orderCheckoutRequests = checkoutSession.orderCheckoutList;

        List<Long> bookIds = orderCheckoutRequests.stream()
                .map(OrderCheckoutRequest::bookId)
                .toList();

        List<BookOrderResponse> books = bookService.getOrderBookByBookIds(bookIds);

        if (books.size() != bookIds.size()) {
            throw new OrderBookNotFoundException();
        }

        List<OrderBookResponse> bookItems = getOrderBookMappingQuantity(books, orderCheckoutRequests);

        //책 * 수량 최종 금액
        int finalAmount = bookItems.stream()
                .mapToInt(item -> item.salePrice() * item.quantity())
                .sum();

        String orderName = createOrderName(bookItems);

        List<DeliveryDateResponse> deliveryDateResponses = getDeliveryDates();
        List<PackagingOptionResponse> packagingOptions = packagingOptionService.getPackagingOptions();

        return OrderCheckoutResponse.from(
                bookItems,
                orderName,
                finalAmount,
                deliveryDateResponses,
                packagingOptions);
    }


    // 단순 배송날짜 계산
    private List<DeliveryDateResponse> getDeliveryDates() {
        LocalDate date = LocalDate.now();

        List<DeliveryDateResponse> deliveryDateResponseList = new ArrayList<>();

        for (int i = 2; i <= 7; i++) {
            deliveryDateResponseList.add(new DeliveryDateResponse(
                    date.plusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    date.plusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd (E)", Locale.KOREAN))
            ));
        }
        return deliveryDateResponseList;
    }

    // 담은 책 종류에 따라 주문명 생성
    private String createOrderName(List<OrderBookResponse> bookItems) {
        if (bookItems.isEmpty()) {
            return "";
        }

        if (bookItems.size() == 1 && bookItems.getFirst().quantity() == 1) {
            return bookItems.getFirst().bookTitle();
        } else {
            int num = bookItems.stream().mapToInt(OrderBookResponse::quantity).sum() - 1;
            return bookItems.getFirst().bookTitle() + " 외 " + num + "권";
        }
    }

    // 주문 도서 리스트 생성
    private List<OrderBookResponse> getOrderBookMappingQuantity(List<BookOrderResponse> books,
                                                                List<OrderCheckoutRequest> orderCheckoutRequests) {

        Map<Long, BookOrderResponse> bookMap = books.stream() //O(N) -> O(1)
                .collect(Collectors.toMap(BookOrderResponse::id, Function.identity()));

        return orderCheckoutRequests.stream()
                .map(req -> {
                    BookOrderResponse bookOrderResponse = bookMap.get(req.bookId());

                    return OrderBookResponse.from(bookOrderResponse, req.quantity());
                })
                .toList();
    }
}

