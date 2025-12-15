package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookStockChangeRequest;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewService;
import com.nhnacademy._vidiabookstoreservice.cart.service.CartService;
import com.nhnacademy._vidiabookstoreservice.global.client.CouponClient;
import com.nhnacademy._vidiabookstoreservice.order.domain.*;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.OrderStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.*;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.*;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentConfirmRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentCancelResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.TossPaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderAmountMismatchException;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderFailedException;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.exception.PaymentNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.mq.producer.OrderMessageProducer;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.order.service.*;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointUseRequest;
import com.nhnacademy._vidiabookstoreservice.point.exception.NotEnoughPointException;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final UserService userService;
    private final BookService bookService;
    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final PaymentService<TossPaymentResponse> paymentService;
    private final PackagingService packagingService;
    private final PackagingOptionService packagingOptionService;
    private final PointCommandService pointCommandService;
    private final ReviewService reviewService;
    private final CouponClient couponClient;
    private final RabbitTemplate rabbitTemplate;
    private final OrderMessageProducer orderMessageProducer;
    private final CartService cartService;
    private final StringRedisTemplate bestsellerRedisTemplate;

    @Override
    public OrderCreateResponse saveOrder(Long userId, OrderCreateRequest request) {
        User user = null;
        if (userId != null) {
            user = userService.getUserById(userId);
        }

        validateOrder(user, request); // 오류 안걸리면 검증 성공

        // 검증 성공하면 주문 저장
        Order order = Order.builder()
                .user(user)
                .recipientName(request.recipientName())
                .addressRoadname(request.addressRoadname())
                .addressDetail(request.addressDetail())
                .zipCode(request.zipCode())
                .recipientPhone(request.recipientPhone())
                .deliveryRequest(request.deliveryRequest())
                .orderPassword(request.orderPassword())
                .couponDiscount(request.couponDiscount())
                .pointUsed(request.pointUsed())
                .deliveryDate(request.deliveryDate())
                .totalPrice(request.totalPrice() + request.deliveryCost() + request.packagingCost())
                .payPrice(request.payPrice())
                .build();

        Order savedOrder = orderRepository.save(order);

        try {
            List<OrderItemRequest> orderItemRequests = request.orderItems().stream()
                    .map(OrderItemRequest::from)
                    .toList();

            useCouponAndDecreaseStockAndPoint(savedOrder, orderItemRequests, request.couponId(), request.pointUsed());

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

                        packagingService.addPackaging(packaging);

                    }
                }
                orderMessageProducer.sendDelayedCancelMessage(savedOrder.getOrderId());
            }
            return new OrderCreateResponse(savedOrder.getOrderId());

        } catch (Exception e) {
            if (user != null) {
                sendRollBackCoupon(savedOrder.getOrderId());
            }

            throw new OrderFailedException(e.getMessage());
        }
    }

    /**
     결제 확정 및 결제 저장, 주문상태 완료로 변경, 장바구니에서 주문아이템 삭제
     */
    @Override
    public PaymentResponse payAndCompleteOrder(Long orderId, PaymentConfirmRequest confirmRequest, Long userId) {


        Order order = getOrder(orderId);

        try {
            TossPaymentResponse tossPaymentResponse = paymentService.confirmPayment(confirmRequest.paymentKey(), confirmRequest.orderId(), confirmRequest.amount());

            PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.from(order, tossPaymentResponse, tossPaymentResponse.totalAmount());

            PaymentResponse paymentResponse = paymentService.savePayment(paymentCreateRequest);

            order.setOrderStatus(OrderStatus.PAID);

            List<Long> orderBooks = order.getOrderItems().stream().map(OrderItem::getBook).map(Book::getId).toList();
            cartService.removeItemByOrder(userId, orderBooks);

            List<OrderItem> orderItems = order.getOrderItems();

            orderItems.stream().forEach(orderItem ->
                    bestsellerRedisTemplate.opsForZSet().incrementScore("bestseller", orderItem.getBook().getId().toString(), orderItem.getQuantity())
            ); // TODO 의미가? 분리하는게 비동기 event 처리하기

            return paymentResponse;

        } catch (Exception e) {
            cancelCouponAndDecreaseStockAndPoint(order, confirmRequest);

            throw new OrderFailedException("결제실패" + e.getMessage());
        }

        //TODO orders 테이블 주문상태 수정해야하는데 가상계좌가 들어오니 status 뜯어보고 그거에 따라 바꿔줘야하나?
        // 가상계좌면 계좌정보 담아서 보내야함?. 근데 일단 pass
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderResponse(Long orderId) {
        Order order = orderRepository.findByOrderIdWithAll(orderId).orElseThrow(
                () -> new OrderNotFoundException(orderId)
        );

        return OrderResponse.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findByOrderId(orderId).orElseThrow(
                () -> new OrderNotFoundException(orderId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderPreviewResponse> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findAllByUser_UserId(userId);

        List<Long> ordersIds = orders.stream()
                .flatMap(order -> order.getOrderItems().stream()) // 모든 주문의 OrderItem 리스트를 하나의 스트림으로 합치고
                .map(OrderItem::getOrderItemId) // OrderItem에서 ID만 추출
                .toList();

        List<Long> writtenReview = reviewService.getReviewedOrderItemIdList(ordersIds);

        Set<Long> reviewedItemIds = new HashSet<>(writtenReview);

        return orders.stream().map(
                order -> OrderPreviewResponse.from(order, reviewedItemIds))
                .toList();
    }



    //쿠폰 사용, 포인트 사용, 도서 차감
    public void useCouponAndDecreaseStockAndPoint(Order order, List<OrderItemRequest> itemRequests, Long couponId, int pointUsed) {
        if (order.getUser() != null) {
            if (couponId != null) {
                CouponUseRequest couponUseRequest = new CouponUseRequest(order.getOrderId(), couponId);
                couponClient.useCoupon(order.getUser().getUserId(), couponUseRequest);
            }

            PointUseRequest pointUseRequest = new PointUseRequest(order.getOrderId(), pointUsed);
            pointCommandService.use(pointUseRequest, order.getUser().getUserId());
        }

        List<BookStockChangeRequest> bookStockDecreaseRequestList = itemRequests.stream()
                .map(item -> new BookStockChangeRequest(item.bookId(), item.quantity()))
                .toList();

        bookService.decreaseStock(bookStockDecreaseRequestList);
    }

    //쿠폰 상태 복구, 포인트 사용 복구, 도서 차감 복구, 결제실패 요청, 주문 상태변경
    private void cancelCouponAndDecreaseStockAndPoint(Order order, PaymentConfirmRequest confirmRequest) {
        if (order.getUser() != null) {
            sendRollBackCoupon(order.getOrderId());

            pointCommandService.cancelUse(order.getOrderId(), order.getUser().getUserId());
        }

        List<OrderItemRequest> orderItemRequests = orderItemService.getOrderItemRequests(order);

        List<BookStockChangeRequest> bookStockDecreaseRequestList = orderItemRequests.stream()
                .map(item -> new BookStockChangeRequest(item.bookId(), item.quantity()))
                .toList();
        bookService.increaseStock(bookStockDecreaseRequestList);

        if (confirmRequest != null) { // 결제과정에서 성공해서 paymentKey값 있을때만
            // 멱등성 있어서 취소할 결제가 없어도 안전하게 무시되니 부르는게 안전성 굳
            paymentService.cancelPayment(confirmRequest.paymentKey(), "결제 확정 및 처리 실패", order.getPayPrice());
        }

        order.setOrderStatus(OrderStatus.REFUNDED);
    }

    private void sendRollBackCoupon(Long orderId) {
        rabbitTemplate.convertAndSend("coupon4.exchange", "coupon4.use.rollback", orderId);
    }


    // 15분동안 미결제 시 주문 취소 및 사용 아이템 복구
    @Override
    public void cancelOrderIfPending(Long orderId) {
        Order order = getOrder(orderId);

        if (order.getOrderStatus() == OrderStatus.PENDING) {

            PaymentConfirmRequest confirmRequest = null;

            try {
                PaymentCancelResponse paymentKey = paymentService.getPaymentKey(order.getOrderId());
                confirmRequest = new PaymentConfirmRequest(paymentKey.paymentKey(), paymentKey.orderId(), paymentKey.amount());
                cancelCouponAndDecreaseStockAndPoint(order, confirmRequest);

            } catch (PaymentNotFoundException e) {
                cancelCouponAndDecreaseStockAndPoint(order, null);
            }
        }
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = getOrder(orderId);

        // OrderStatus -> PAID(1) 일때는 결제 취소해야함
        if (order.getOrderStatus() == OrderStatus.PAID) {
            Payment payment = paymentService.getPaymentEntity(orderId);

            TossPaymentResponse tossPaymentResponse = paymentService.cancelPayment(payment.getPaymentKey(), "배송 전 취소",  payment.getAmount());

            PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.from(order, tossPaymentResponse, tossPaymentResponse.cancels().getLast().cancelAmount());

            paymentService.savePayment(paymentCreateRequest);
        }

        // OrderStatus -> PENDING(0)에도 상태 변경 해줘야함 (if문 밖으로 뺌)
        order.setOrderStatus(OrderStatus.REFUNDED); // 취소로 변경
        order.setDeliveryStatus(DeliveryStatus.CANCELED); // 취소로 변경

        List<BookStockChangeRequest> requests = order.getOrderItems().stream()
                .map(item -> new BookStockChangeRequest(item.getBook().getId(), item.getQuantity()))
                .toList();
        bookService.increaseStock(requests);

        // TODO 혹시 주문취소 시 포인트 환불 요청이 빠졌다면.. cancelUse() 호출해주세요!
//        pointCommandService.cancelUse(orderId, order.getUser().getUserId());
    }

    @Override
    public Boolean validateGuest(OrderTrackingRequest orderTrackingRequest) {
        Order order = getOrder(orderTrackingRequest.orderId());

        return order.getOrderPassword().equals(orderTrackingRequest.orderPassword());
    }

    @Override
    public void changeOrderStatus(Long orderId, ConfirmStatus confirmStatus) {
        Order order = getOrder(orderId);

        for (OrderItem orderItem : order.getOrderItems()) {
            orderItemService.changeStatusOrderItem(orderItem.getOrderItemId(), confirmStatus);
        }
    }

    @Override
    public void changeOrderStatus_ByUser(Long orderId, ConfirmStatus confirmStatus) {
        Order order = getOrder(orderId);

        for (OrderItem orderItem : order.getOrderItems()) {
            orderItemService.changeStatusOrderItem_byUser(orderItem.getOrderItemId(), confirmStatus);
        }

        //TODO 포인트 지급도 해야됨 - 서비스 부르기(오더Entity) -> 추가했습니다.
        pointCommandService.reward(order);
    }

    /**
     * 사용 전 선택된 상품 아이템 가격, 배송비 정책, 선택된 포장지 가격, 쿠폰 할인금액 검증
     * @param user
     * @param request
     */
    private void validateOrder(User user, OrderCreateRequest request) {
        int serverItemPrice = 0; // 서버에서 계산할 도서 총 금액
        int serverPackagingPrice = 0; // 서버에서 계산할 포장비 총 금액

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderCreateRequest.ItemRequestDto itemDto : request.orderItems()) {

            Book book = bookService.getBookEntity(itemDto.bookId());

            serverItemPrice += (book.getPriceSales() * itemDto.quantity());

            orderItems.add(OrderItem.builder()
                    .book(book)
                    .quantity(itemDto.quantity()) // 사용자가 선택한 수량
                    .salePrice(book.getPriceSales()) // 도서에서 가져온 값
                    .confirmStatus(ConfirmStatus.UNCONFIRMED)
                    .build()
            );

            for (Long packagingOptionId : itemDto.packagingOptionIds()) {
                PackagingOption packagingOption = packagingOptionService.getByPackagingOptionId(packagingOptionId);

                serverPackagingPrice += packagingOption.getPrice();
            }
        }

        int serverDeliveryPrice = 0; // 서버에서 계산할 배송비 총 금액
        serverDeliveryPrice = serverItemPrice + serverPackagingPrice < 50000 ? 3000 : 0;

        int serverCouponPrice = 0; // 서버에서 계산할 쿠폰 할인 총 금액

        if (user != null) { // 회원이면 회원 정보 조회, 쿠폰 검증
            List<CouponCalculationRequest.ItemInfo> itemInfos = orderItems.stream()
                    .map(item -> new CouponCalculationRequest.ItemInfo(
                            item.getBook().getId(),
                            item.getBook().getCategory().getKdcCode(),
                            item.getBook().getPriceSales(),
                            item.getQuantity()
                    )).toList();

            if (request.couponId() != null) {
                CouponCalculationRequest couponCalculationRequest = new CouponCalculationRequest(
                        request.couponId(),
                        itemInfos
                );
                serverCouponPrice = couponClient.calculateCoupons(Objects.requireNonNull(user).getUserId(), couponCalculationRequest);
            }

        }

        int serverPointPrice = request.pointUsed(); // 서버에서 계산할 유저 할인 가능 금액
        if (user != null) {
            if (request.pointUsed() > user.getPoint()) {
                throw new NotEnoughPointException();
            }
        } else {
            if (request.pointUsed() != 0) {
                throw new NotEnoughPointException();
            }
        }


        int finalTotalPrice = serverItemPrice + serverPackagingPrice + serverDeliveryPrice - serverCouponPrice - serverPointPrice;

        if (finalTotalPrice != request.payPrice()) {
            throw new OrderAmountMismatchException();
        }
    }
}
