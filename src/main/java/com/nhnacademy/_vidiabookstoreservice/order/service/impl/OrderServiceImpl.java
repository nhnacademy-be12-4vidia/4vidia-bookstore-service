package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookStockChangeRequest;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.book.service.DiscountPolicyService;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewService;
import com.nhnacademy._vidiabookstoreservice.cart.service.CartService;
import com.nhnacademy._vidiabookstoreservice.global.client.CouponClient;
import com.nhnacademy._vidiabookstoreservice.order.domain.*;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.OrderStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.event.BestSellerUpdateEvent;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.*;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.*;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentConfirmRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentCancelResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.TossPaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.exception.*;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.PaymentNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.mq.producer.OrderMessageProducer;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.order.service.*;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointUseRequest;
import com.nhnacademy._vidiabookstoreservice.point.exception.invalid.PointGuestUseException;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundItemStatus;
import com.nhnacademy._vidiabookstoreservice.refund.repository.RefundItemRepository;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    private final ApplicationEventPublisher eventPublisher;
    private final RefundItemRepository refundItemRepository;
    private final OrderItemViewStatusResolver resolver;
    private final DiscountPolicyService discountPolicyService;

    @Override
    public OrderCreateResponse saveOrder(Long userId, @Valid OrderCreateRequest request) {
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
                .totalBookPrice(request.totalBookPrice())
                .deliveryFee(request.deliveryFee())
                .packagingFee(request.packagingFee())
                .couponDiscount(request.couponDiscount())
                .pointUsed(request.pointUsed())
                .deliveryDate(request.deliveryDate())
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
        // 주문 상태 안보고 왜 결제성공시킴?
        if (!order.getOrderStatus().equals(OrderStatus.PENDING)) {
            throw new OrderFailedException("주문 상태 이상");
        }
        try {
            TossPaymentResponse tossPaymentResponse = paymentService.confirmPayment(confirmRequest.paymentKey(), confirmRequest.orderId(), confirmRequest.amount());

            PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.from(order, tossPaymentResponse, tossPaymentResponse.totalAmount());

            PaymentResponse paymentResponse = paymentService.savePayment(paymentCreateRequest);

            order.setOrderStatus(OrderStatus.PAID);

            List<Long> orderBooks = order.getOrderItems().stream().map(OrderItem::getBook).map(Book::getId).toList();
            cartService.removeItemByOrder(userId, orderBooks);

            eventPublisher.publishEvent(new BestSellerUpdateEvent(orderId));

            return paymentResponse;

        } catch (Exception e) {
            cancelCouponAndDecreaseStockAndPoint(order, confirmRequest);

            throw new OrderFailedException("결제실패" + e.getMessage());
        }

        // 추가 1. orders 테이블 주문상태 수정해야하는데 가상계좌가 들어오니 status 뜯어보고 그거에 따라 바꿔줘야하나?
        // 추가 2. 가상계좌면 계좌정보 담아서 보여줘야함?. 근데 일단 pass
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderResponse(Long userId, Long orderId) {
        Order order = orderRepository.findByOrderIdWithAll(orderId).orElseThrow(
                () -> new OrderNotFoundException(orderId)
        );

        if (!Objects.equals(order.getUser().getUserId(), userId)) {
            throw new OrderAccessDeniedException(orderId, "주문 조회 권한이 없습니다.");
        }

        return OrderResponse.from(order);
    }

    @Override
    public OrderResponse getGuestOrderResponse(OrderTrackingRequest orderTrackingRequest) {
        Order order = getOrder(orderTrackingRequest.orderId());

        if (order.getUser() != null) {
            throw new OrderAccessDeniedException(orderTrackingRequest.orderId(), "회원은 로그인 후 조회 가능합니다.");
        }
        if (!order.getOrderPassword().equals(orderTrackingRequest.orderPassword())) {
            throw new InvalidOrderPasswordException(orderTrackingRequest.orderId());
        }
        return OrderResponse.from(order);
    }

    @Override
    public OrderAmountResponse getOrderPayPrice(Long orderId) {
        Order order = orderRepository.findByOrderIdWithAll(orderId).orElseThrow(
                () -> new OrderNotFoundException(orderId)
        );

        return new OrderAmountResponse(order.getTotalBookPrice() +
                order.getPackagingFee() +
                order.getDeliveryFee() -
                order.getCouponDiscount() -
                order.getPointUsed()
                );
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
    public Page<OrderPreviewResponse> getOrdersByUserId(Long userId, String status, Pageable pageable) {
        Page<Order> orderPage;

        if (status == null || status.equalsIgnoreCase("ALL")) { // 전체 조회
            orderPage = orderRepository.findAllByUser_UserId(userId, pageable);
        } else if (status.equalsIgnoreCase("REFUND_REQUEST")) { // 반품/교환
            orderPage = orderRepository.findRefundRequestsByUserId(userId, RefundItemStatus.PROCESS, pageable);
        } else {
            try {
                DeliveryStatus deliveryStatus = DeliveryStatus.valueOf(status.toUpperCase());
                orderPage = orderRepository.findAllByUser_UserIdAndDeliveryStatus(userId, deliveryStatus, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 status: {}", status);
                // 잘못된 상태값이 오면 전체 조회로 fallback
                orderPage = orderRepository.findAllByUser_UserId(userId, pageable);
            }
        }

        List<Long> ordersItemIds = orderPage.stream()
                .flatMap(order -> order.getOrderItems().stream()) // 모든 주문의 OrderItem 리스트를 하나의 스트림으로 합치고
                .map(OrderItem::getOrderItemId) // OrderItem에서 ID만 추출
                .toList();

        List<Long> writtenReview = ordersItemIds.isEmpty() ? Collections.emptyList() : reviewService.getReviewedOrderItemIdList(ordersItemIds);
        List<RefundItem> refundItems = ordersItemIds.isEmpty() ? Collections.emptyList() : refundItemRepository.findByOrderItem_OrderItemId(ordersItemIds);

        // 문제 원인: 리스트에 같은 OrderItem에 대한 반품 내역이 여러 개(거절됨, 재신청됨 등) 있을 때 HashSet에 다 들어가서 랜덤으로 뽑힘.
        // 해결: Map을 이용해 OrderItemId 별로 '가장 최근(ID가 큰)' 반품 내역 하나만 남김
        Map<Long, RefundItem> latestRefundMap = refundItems.stream()
                .collect(Collectors.toMap(
                        ri -> ri.getOrderItem().getOrderItemId(), // Key: 주문아이템 ID
                        ri -> ri, // Value: 반품 객체
                        (existing, replacement) -> { // 중복 발생 시 로직
                            // ID가 더 큰 것(나중에 생성된 것)을 선택 -> 최신 상태 반영
                            return existing.getRefundItemId() > replacement.getRefundItemId() ? existing : replacement;
                        }
                ));

        Set<Long> reviewedItemIds = new HashSet<>(writtenReview);
        // Map의 values()만 뽑아서 Set으로 만듦 (이제 중복 없음)
        Set<RefundItem> refundItemsSet = new HashSet<>(latestRefundMap.values());

        return orderPage.map(order -> OrderPreviewResponse.from(order, reviewedItemIds, refundItemsSet, resolver));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderCountResponse getOrderCounts(Long userId) {
        return orderRepository.countOrdersByUserId(
                userId,
                DeliveryStatus.WAITING,
                DeliveryStatus.SHIPPING,
                DeliveryStatus.DELIVERED,
                DeliveryStatus.CANCELED
        );
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
            int payPrice = order.getTotalBookPrice() + order.getPackagingFee() + order.getDeliveryFee() - order.getCouponDiscount() - order.getPointUsed();
            try {
                paymentService.cancelPayment(confirmRequest.paymentKey(), "결제 확정 및 처리 실패", payPrice);
            } catch (PaymentCancelException e) {
                throw new OrderRollbackFailedException(e.getMessage());
            }
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

            PaymentConfirmRequest confirmRequest;

            try {
                PaymentCancelResponse paymentKey = paymentService.getPaymentKey(order.getOrderId());
                confirmRequest = new PaymentConfirmRequest(paymentKey.paymentKey(), paymentKey.orderId(), paymentKey.amount());
                cancelCouponAndDecreaseStockAndPoint(order, confirmRequest);

            } catch (PaymentNotFoundException e) {
                cancelCouponAndDecreaseStockAndPoint(order, null);
            }
        }
    }

    // 배송 전 취소, 결제 중 실패
    @Override
    public void cancelOrder(Long orderId, String message) {
        Order order = getOrder(orderId);

        // OrderStatus -> PAID(1) 일때는 결제 취소해야함
        if (order.getOrderStatus() == OrderStatus.PAID) {
            Payment payment = paymentService.getPaymentEntity(orderId);

            TossPaymentResponse tossPaymentResponse;
            PaymentCreateRequest paymentCreateRequest;
            try {
                tossPaymentResponse = paymentService.cancelPayment(payment.getPaymentKey(), message,  payment.getAmount());

                paymentCreateRequest = PaymentCreateRequest.from(order, tossPaymentResponse, tossPaymentResponse.cancels().getLast().cancelAmount());

                paymentService.savePayment(paymentCreateRequest);
            } catch (PaymentConfirmException e) { // 실제 결제는 취소인데 상태가 업데이트 안되었을때
                paymentCreateRequest = new PaymentCreateRequest(order, "CANCELED", payment.getPayMethod(), payment.getAmount(), payment.getPaymentKey(), payment.getSendOrderId());
                paymentService.savePayment(paymentCreateRequest);
            }
        }

        // OrderStatus -> PENDING(0)에도 상태 변경 해줘야함 (if문 밖으로 뺌)
        order.setOrderStatus(OrderStatus.REFUNDED); // 취소로 변경
        order.setDeliveryStatus(DeliveryStatus.CANCELED); // 취소로 변경

        List<BookStockChangeRequest> requests = order.getOrderItems().stream()
                .map(item -> new BookStockChangeRequest(item.getBook().getId(), item.getQuantity()))
                .toList();
        bookService.increaseStock(requests);

        if (order.getPointUsed() != 0) {
            pointCommandService.cancelUse(orderId, order.getUser().getUserId());
        }
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

        List<Long> orderItemIds = order.getOrderItems().stream().map(OrderItem::getOrderItemId).toList();

        List<RefundItem> refundItems = refundItemRepository.findAlLByOrderItem_OrderItemIdInAndRefundItemStatus(orderItemIds, RefundItemStatus.APPROVED);

        List<Long> refundIds = refundItems.stream().map(item -> item.getOrderItem().getOrderItemId()).toList();

        for (OrderItem orderItem : order.getOrderItems()) {
            if (refundIds.contains(orderItem.getOrderItemId())) {
                continue;
            }
            orderItemService.changeStatusOrderItem_byUser(orderItem.getOrderItemId(), confirmStatus);
        }

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

            Integer calculatedPrice = discountPolicyService.calculateSalesPrice(book.getPriceStandard(), book.getCategory());

            serverItemPrice += (calculatedPrice * itemDto.quantity());

            orderItems.add(OrderItem.builder()
                    .book(book)
                    .quantity(itemDto.quantity()) // 사용자가 선택한 수량
                    .salePrice(calculatedPrice) // 도서에서 가져온 값
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
                            item.getSalePrice(),
                            item.getQuantity()
                    )).toList();

            if (request.couponId() != null) {
                CouponCalculationRequest couponCalculationRequest = new CouponCalculationRequest(
                        request.couponId(),
                        itemInfos
                );
                CouponCalculationResponse couponCalculationResponse = couponClient.calculateCoupons(Objects.requireNonNull(user).getUserId(), couponCalculationRequest);

                serverCouponPrice = couponCalculationResponse.discountPrice();
            }

        }

        int serverPointPrice = 0;
        if(user!=null){
            // 주문 직전 포인트 보정
            pointCommandService.expireIfNeeded(user.getUserId());

            pointCommandService.validateUsablePoint(user.getUserId(), request.pointUsed());

            serverPointPrice = request.pointUsed();

        }else{
            if(request.pointUsed()!=0){
                throw new PointGuestUseException();
            }
        }

        int finalTotalPrice = serverItemPrice + serverPackagingPrice + serverDeliveryPrice - serverCouponPrice - serverPointPrice;

        int reqPayPrice = request.totalBookPrice() + request.packagingFee() + request.deliveryFee() - request.couponDiscount() - request.pointUsed();

        if (finalTotalPrice != reqPayPrice) {
            throw new OrderAmountMismatchException();
        }
    }
}
