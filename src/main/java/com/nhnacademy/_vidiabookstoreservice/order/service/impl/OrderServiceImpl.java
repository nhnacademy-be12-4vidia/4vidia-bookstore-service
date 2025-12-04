package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.global.client.CouponClient;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.Packaging;
import com.nhnacademy._vidiabookstoreservice.order.domain.PackagingOption;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.OrderStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.CouponRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.*;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderItemService;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import com.nhnacademy._vidiabookstoreservice.order.service.PackagingOptionService;
import com.nhnacademy._vidiabookstoreservice.order.service.PackagingService;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.OrderUserResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final UserService userService;
    private final BookService bookService;
    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final PackagingService packagingService;
    private final PackagingOptionService packagingOptionService;
    private final CouponClient couponClient;

    @Override
    public List<DeliveryDateResponse> getDeliveryDates() {
        LocalDate date = LocalDate.now();

        List<DeliveryDateResponse> deliveryDateResponseList = new ArrayList<>();

        for (int i=2; i<=7; i++) {
            deliveryDateResponseList.add(new DeliveryDateResponse(
                    date.plusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    date.plusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd (E)", Locale.KOREAN))
            ));
        }
        return deliveryDateResponseList;
    }

    @Override
    public OrderCreateResponse saveOrder(Long userId, OrderCreateRequest request) {

        User user = null;
        if (userId != null) {
            user = userService.getUserById(userId);
        }

        //TODO 재고 차감 구현 wow 어떻게하냐


        Order order = Order.builder()
                .user(user)
                .recipientName(request.recipientName())
                .addressRoadname(request.addressRoadname())
                .addressDetail(request.addressDetail())
                .zipCode(request.zipCode())
                .recipientPhone(request.recipientPhone())
                .deliveryRequest(request.deliveryRequest())
                .couponDiscount(request.couponDiscount())
                .pointUsed(request.pointUsed())
                .deliveryDate(request.deliveryDate())
                .totalPrice(request.totalPrice() + request.deliveryCost() + request.packagingCost())
                .payPrice(request.payPrice())
                .build();

        Order savedOrder = orderRepository.save(order);

        for (OrderCreateRequest.ItemRequestDto itemDto : request.orderItems()) {
            Book book = bookService.getBookEntity(itemDto.bookId());

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .book(book)
                    .quantity(itemDto.quantity())
                    .salePrice(itemDto.salePrice())
                    .confirmStatus(ConfirmStatus.UNCONFIRMED)
                    .build();

            savedOrder.getOrderItems().add(orderItem);

            OrderItem savedOrderItem = orderItemService.addOrderItem(orderItem);

            for (Long packagingOptionId : itemDto.packagingOptionIds()) {
                if (packagingOptionId != 0) {
                    PackagingOption packagingOption = packagingOptionService.getByPackagingOptionId(packagingOptionId);


                    Packaging packaging = Packaging.builder()
                            .orderItem(savedOrderItem)
                            .packagingOption(packagingOption)
                            .build();

                    packagingService.addPacakging(packaging);
                }
            }
        }

        return new OrderCreateResponse(savedOrder.getOrderId());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderResponse(Long orderId) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow(
                () -> new OrderNotFoundException(orderId)
        );

        return OrderResponse.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow(
                () -> new OrderNotFoundException(orderId)
        );

        return order;
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow(
                () -> new OrderNotFoundException(orderId)
        );

        order.setOrderStatus(orderStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderPreviewResponse> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findAllByUser_UserId(userId);

        return orders.stream().map(OrderPreviewResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true) //주문화면에 보낼 값
    public OrderCheckoutResponse getOrderCheckoutResponse(Long userId, List<OrderCheckoutRequest> orderCheckoutRequests) {

        OrderUserResponse orderUserResponse = null;
        if (userId != null) {
            orderUserResponse = userService.getOrderUser(userId);
        } else {
            orderUserResponse = new OrderUserResponse("", "", "", 0, null);
        }

        List<Long> bookIds = orderCheckoutRequests.stream()
                .map(OrderCheckoutRequest::bookId)
                .toList();

        List<BookOrderResponse> books = bookService.getOrderBookByBookIds(bookIds);

        Map<Long, BookOrderResponse> bookMap = books.stream() //O(N) -> O(1)
                .collect(Collectors.toMap(BookOrderResponse::id, Function.identity()));

        List<OrderBookResponse> bookItems = orderCheckoutRequests.stream()
                .map(req -> {
                    BookOrderResponse bookOrderResponse = bookMap.get(req.bookId());

                    return OrderBookResponse.from(bookOrderResponse, req.quantity());
                })
                .toList();


        //책 * 수량 최종 금액
        int finalAmount = bookItems.stream()
                .mapToInt(item -> item.salePrice() * item.quantity())
                .sum();

        //담은 책 종류에 따라 주문명 변경
        String orderName = "";
        if (bookItems.size() == 1) {
            orderName = bookItems.getFirst().bookTitle();
        }else {
            orderName = bookItems.getFirst().bookTitle() + " 외 " + (bookItems.size() - 1) + "권";
        }

        List<Long> categoryIds = books.stream()
                .map(BookOrderResponse::category)
                .toList();

//        CouponRequest couponRequest = new CouponRequest(finalAmount, bookIds, categoryIds);
//        List<OrderPageCouponResponse> orderPageCouponResponses = couponClient.getUserCoupons(couponRequest);

        List<DeliveryDateResponse> deliveryDateResponses = getDeliveryDates();
        List<PackagingOptionResponse> packagingOptions =  packagingOptionService.getPackagingOptions();



        return OrderCheckoutResponse.from(orderUserResponse, bookItems, orderName, finalAmount, /*orderPageCouponResponses*/null, deliveryDateResponses, packagingOptions);
    }
}
