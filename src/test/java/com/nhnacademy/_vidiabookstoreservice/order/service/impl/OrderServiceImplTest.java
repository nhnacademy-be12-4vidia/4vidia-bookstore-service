package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.order.domain.*;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.OrderStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.event.BestSellerUpdateEvent;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.CouponUseRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderItemRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderTrackingRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.CouponCalculationResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderAmountResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderCreateResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentConfirmRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentCancelResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.TossPaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.exception.*;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.PaymentNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointUseRequest;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundItemStatus;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    OrderServiceImpl orderService;

    @Mock private com.nhnacademy._vidiabookstoreservice.user.service.UserService userService;
    @Mock private com.nhnacademy._vidiabookstoreservice.book.service.BookService bookService;
    @Mock private com.nhnacademy._vidiabookstoreservice.order.service.OrderItemService orderItemService;
    @Mock private com.nhnacademy._vidiabookstoreservice.order.service.PaymentService paymentService;
    @Mock private com.nhnacademy._vidiabookstoreservice.order.service.PackagingOptionService packagingOptionService;
    @Mock private com.nhnacademy._vidiabookstoreservice.order.service.PackagingService packagingService;
    @Mock private com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService pointCommandService;
    @Mock private com.nhnacademy._vidiabookstoreservice.global.client.CouponClient couponClient;
    @Mock private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    @Mock private com.nhnacademy._vidiabookstoreservice.order.mq.producer.OrderMessageProducer orderMessageProducer;
    @Mock private com.nhnacademy._vidiabookstoreservice.cart.service.CartService cartService;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;
    @Mock private com.nhnacademy._vidiabookstoreservice.refund.repository.RefundItemRepository refundItemRepository;
    @Mock private com.nhnacademy._vidiabookstoreservice.book.service.DiscountPolicyService discountPolicyService;

    @Test
    @DisplayName("모든 검증 통과 후 주문 저장 성공")
    void saveOrder_Success() {
        Long userId = 1L;
        Long bookId = 100L;
        Long packagingOptionId = 1L;
        Long couponId = 50L;
        int salePrice = 10000;
        int quantity = 1;
        int packagingPrice = 1000;
        int deliveryFee = 3000;
        int couponDiscount = 2000;
        int pointUsed = 1000;

        User mockUser = mock(User.class);
        given(mockUser.getUserId()).willReturn(userId);
        given(userService.getUserById(userId)).willReturn(mockUser);

        Category mockCategory = mock(Category.class);
        given(mockCategory.getKdcCode()).willReturn("100");

        Book mockBook = mock(Book.class);
        given(mockBook.getId()).willReturn(bookId);
        given(mockBook.getPriceStandard()).willReturn(salePrice);
        given(mockBook.getCategory()).willReturn(mockCategory);
        given(bookService.getBookEntity(bookId)).willReturn(mockBook);

        given(discountPolicyService.calculateSalesPrice(anyInt(), eq(mockCategory))).willReturn(salePrice);

        PackagingOption mockOption = mock(PackagingOption.class);
        given(mockOption.getPrice()).willReturn(packagingPrice);
        given(packagingOptionService.getByPackagingOptionId(packagingOptionId)).willReturn(mockOption);

        given(couponClient.calculateCoupons(anyLong(), any()))
                .willReturn(new CouponCalculationResponse(couponDiscount));

        OrderCreateRequest request = new OrderCreateRequest(
                "수령인", "도로명주소", "상세주소", "12345", "010-1234-5678",
                "문앞에", "password123", LocalDate.now().plusDays(2),
                salePrice, deliveryFee, packagingPrice, couponDiscount, pointUsed,
                List.of(new OrderCreateRequest.ItemRequestDto(bookId, quantity, salePrice, List.of(packagingOptionId))),
                couponId
        );

        Order savedOrder = Order.builder().build();
        ReflectionTestUtils.setField(savedOrder, "orderId", 500L);
        ReflectionTestUtils.setField(savedOrder, "user", mockUser);
        ReflectionTestUtils.setField(savedOrder, "orderItems", new ArrayList<>());

        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

        OrderItem mockOrderItem = mock(OrderItem.class);
        given(orderItemService.addOrderItem(any())).willReturn(mockOrderItem);

        doNothing().when(couponClient).useCoupon(eq(userId), any(CouponUseRequest.class));
        doNothing().when(pointCommandService).use(any(PointUseRequest.class), eq(userId));
        doNothing().when(bookService).decreaseStock(anyList());

        OrderCreateResponse response = orderService.saveOrder(userId, request);

        assertThat(response.orderId()).isEqualTo(500L);
        verify(orderRepository).save(any(Order.class));
        verify(orderMessageProducer).sendDelayedCancelMessage(500L);
    }

    @Test
    @DisplayName("주문 검증 실패: 요청 금액과 실제 계산 금액 불일치 시 예외 발생")
    void saveOrder_Fail_AmountMismatch() {
        Long userId = 1L;
        Long bookId = 100L;
        Long packagingOptionId = 1L;
        Long couponId = 50L;

        int requestSalePrice = 10000;
        int realDbPrice = 20000;

        int quantity = 1;
        int packagingPrice = 1000;
        int deliveryFee = 3000;
        int couponDiscount = 2000;
        int pointUsed = 1000;

        User mockUser = mock(User.class);
        given(mockUser.getUserId()).willReturn(userId);
        given(userService.getUserById(userId)).willReturn(mockUser);

        Category mockCategory = mock(Category.class);
        given(mockCategory.getKdcCode()).willReturn("100");

        Book mockBook = mock(Book.class);
        given(mockBook.getId()).willReturn(bookId);
        given(mockBook.getPriceStandard()).willReturn(realDbPrice);
        given(mockBook.getCategory()).willReturn(mockCategory);
        given(bookService.getBookEntity(bookId)).willReturn(mockBook);

        given(discountPolicyService.calculateSalesPrice(anyInt(), eq(mockCategory))).willReturn(realDbPrice);

        PackagingOption mockOption = mock(PackagingOption.class);
        given(mockOption.getPrice()).willReturn(packagingPrice);
        given(packagingOptionService.getByPackagingOptionId(packagingOptionId)).willReturn(mockOption);

        given(couponClient.calculateCoupons(anyLong(), any()))
                .willReturn(new CouponCalculationResponse(couponDiscount));

        OrderCreateRequest request = new OrderCreateRequest(
                "수령인", "도로명주소", "상세주소", "12345", "010-1234-5678",
                "문앞에", "password123", LocalDate.now().plusDays(2),
                requestSalePrice,
                deliveryFee, packagingPrice, couponDiscount, pointUsed,
                List.of(new OrderCreateRequest.ItemRequestDto(bookId, quantity, requestSalePrice, List.of(packagingOptionId))),
                couponId
        );

        assertThatThrownBy(() -> orderService.saveOrder(userId, request))
                .isInstanceOf(OrderAmountMismatchException.class);

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderMessageProducer, never()).sendDelayedCancelMessage(anyLong());
    }

    @Test
    @DisplayName("payAndCompleteOrder - 성공: 결제 승인 및 장바구니 삭제 확인")
    void payAndCompleteOrder_Success() {
        Long orderId = 1L;
        Long userId = 10L;
        Long bookId = 100L;

        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest("payKey", "orderId", 15000);

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "userId", userId);

        Book book = Book.builder().build();
        ReflectionTestUtils.setField(book, "id", bookId);
        ReflectionTestUtils.setField(book, "title", "테스트 도서");

        OrderItem item = OrderItem.builder().build();
        ReflectionTestUtils.setField(item, "book", book);

        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "orderId", orderId);
        ReflectionTestUtils.setField(order, "user", user);
        ReflectionTestUtils.setField(order, "orderItems", new ArrayList<>(List.of(item))); // 리스트 초기화

        ReflectionTestUtils.setField(order, "totalBookPrice", 10000);
        ReflectionTestUtils.setField(order, "packagingFee", 2000);
        ReflectionTestUtils.setField(order, "deliveryFee", 3000);
        ReflectionTestUtils.setField(order, "couponDiscount", 0);
        ReflectionTestUtils.setField(order, "pointUsed", 0);
        order.setOrderStatus(OrderStatus.PENDING);

        TossPaymentResponse mockTossResponse = mock(TossPaymentResponse.class);
        given(mockTossResponse.status()).willReturn("DONE");
        given(mockTossResponse.method()).willReturn("CARD");
        given(mockTossResponse.paymentKey()).willReturn("payKey");
        given(mockTossResponse.orderId()).willReturn("orderId");
        given(mockTossResponse.totalAmount()).willReturn(15000L);

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(order));
        given(paymentService.confirmPayment(anyString(), anyString(), anyLong())).willReturn(mockTossResponse);
        given(paymentService.savePayment(any())).willReturn(mock(PaymentResponse.class));

        orderService.payAndCompleteOrder(orderId, confirmRequest, userId);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        verify(cartService, times(1)).removeItemByOrder(userId, List.of(bookId));
        verify(eventPublisher, times(1)).publishEvent(any(BestSellerUpdateEvent.class));
    }

    @Test
    @DisplayName("payAndCompleteOrder - 실패: 결제 승인 실패 시 모든 데이터가 정상적으로 롤백")
    void payAndCompleteOrder_Rollback_Success() {
        Long orderId = 1L;
        Long userId = 10L;
        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest("payKey", "orderId", 15000);

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "userId", userId);

        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "orderId", orderId);
        ReflectionTestUtils.setField(order, "user", user);

        ReflectionTestUtils.setField(order, "totalBookPrice", 10000);
        ReflectionTestUtils.setField(order, "packagingFee", 2000);
        ReflectionTestUtils.setField(order, "deliveryFee", 3000);
        ReflectionTestUtils.setField(order, "couponDiscount", 1000);
        ReflectionTestUtils.setField(order, "pointUsed", 0);
        order.setOrderStatus(OrderStatus.PENDING);

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(order));

        given(paymentService.confirmPayment(anyString(), anyString(), anyLong()))
                .willThrow(new RuntimeException("Toss API Error"));

        OrderItemRequest mockItemRequest = new OrderItemRequest(100L, 1, 10000);
        given(orderItemService.getOrderItemRequests(order))
                .willReturn(List.of(mockItemRequest));


        assertThatThrownBy(() -> orderService.payAndCompleteOrder(orderId, confirmRequest, userId))
                .isInstanceOf(OrderFailedException.class)
                .hasMessageContaining("주문 처리 중 오류가 발생했습니다.");

        verify(rabbitTemplate, times(1)).convertAndSend("coupon4.exchange", "coupon4.use.rollback", orderId);
        verify(pointCommandService, times(1)).cancelUse(orderId, userId);
        verify(bookService, times(1)).increaseStock(anyList());
        verify(paymentService, times(1)).cancelPayment("payKey", "결제 확정 및 처리 실패", 14000L);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    @DisplayName("payAndCompleteOrder - 실패: 결제 취소 API마저 실패하면 OrderRollbackFailedException 발생")
    void payAndCompleteOrder_Rollback_FinalFail() {
        Long orderId = 1L;
        Long userId = 10L;
        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest("payKey", "orderId", 15000);

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "userId", userId);

        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "orderId", orderId);
        ReflectionTestUtils.setField(order, "user", user);

        ReflectionTestUtils.setField(order, "totalBookPrice", 10000);
        ReflectionTestUtils.setField(order, "packagingFee", 2000);
        ReflectionTestUtils.setField(order, "deliveryFee", 3000);
        ReflectionTestUtils.setField(order, "couponDiscount", 0);
        ReflectionTestUtils.setField(order, "pointUsed", 0);
        order.setOrderStatus(OrderStatus.PENDING);

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(order));

        given(paymentService.confirmPayment(anyString(), anyString(), anyLong()))
                .willThrow(new RuntimeException("1차 실패: 결제 승인 오류"));

        given(orderItemService.getOrderItemRequests(order))
                .willReturn(new ArrayList<>());

        given(paymentService.cancelPayment(anyString(), anyString(), anyLong()))
                .willThrow(new PaymentCancelException("2차 실패: 결제 취소 서버 오류"));

        assertThatThrownBy(() -> orderService.payAndCompleteOrder(orderId, confirmRequest, userId))
                .isInstanceOf(OrderRollbackFailedException.class)
                .hasMessageContaining("주문 취소 및 환불 처리 중 오류가 발생했습니다.");

        verify(rabbitTemplate, times(1)).convertAndSend("coupon4.exchange", "coupon4.use.rollback", orderId);
        verify(pointCommandService, times(1)).cancelUse(orderId, userId);
    }

    @Test
    @DisplayName("회원 주문 조회 성공 - 본인의 주문을 조회하면 응답을 반환한다")
    void getOrderResponse_Success() {
        // given
        Long userId = 1L;
        Long orderId = 100L;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "userId", userId);

        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "orderId", orderId);
        ReflectionTestUtils.setField(order, "user", user);

        given(orderRepository.findByOrderIdWithAll(orderId)).willReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderResponse(userId, orderId);

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("회원 주문 조회 실패 - 다른 사용자의 주문에 접근하면 예외가 발생한다")
    void getOrderResponse_Fail_AccessDenied() {
        Long loginUserId = 1L;
        Long otherUserId = 2L;
        Long orderId = 100L;

        User otherUser = User.builder().build();
        ReflectionTestUtils.setField(otherUser, "userId", otherUserId);

        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "orderId", orderId);
        ReflectionTestUtils.setField(order, "user", otherUser);

        given(orderRepository.findByOrderIdWithAll(orderId)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderResponse(loginUserId, orderId))
                .isInstanceOf(OrderAccessDeniedException.class)
                .hasMessageContaining("해당 주문에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("회원 주문 조회 실패 - 존재하지 않는 주문번호일 경우 예외가 발생한다")
    void getOrderResponse_Fail_NotFound() {
        Long userId = 1L;
        Long orderId = 999L;

        given(orderRepository.findByOrderIdWithAll(orderId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderResponse(userId, orderId))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("주문 최종 결제 금액 계산 성공")
    void getOrderPayPrice_Success() {
        Long orderId = 1L;

        int totalBookPrice = 20000;
        int packagingFee = 1000;
        int deliveryFee = 3000;
        int couponDiscount = 2000;
        int pointUsed = 1000;

        int expectedPayPrice = totalBookPrice + packagingFee + deliveryFee - couponDiscount - pointUsed;

        Order mockOrder = mock(Order.class);
        given(mockOrder.getTotalBookPrice()).willReturn(totalBookPrice);
        given(mockOrder.getPackagingFee()).willReturn(packagingFee);
        given(mockOrder.getDeliveryFee()).willReturn(deliveryFee);
        given(mockOrder.getCouponDiscount()).willReturn(couponDiscount);
        given(mockOrder.getPointUsed()).willReturn(pointUsed);

        given(orderRepository.findByOrderIdWithAll(orderId)).willReturn(Optional.of(mockOrder));

        OrderAmountResponse response = orderService.getOrderPayPrice(orderId);

        assertThat(response).isNotNull();
        assertThat(response.payPrice()).isEqualTo(expectedPayPrice);
        verify(orderRepository).findByOrderIdWithAll(orderId);
    }

    @Test
    @DisplayName("주문 금액 조회 실패: 존재하지 않는 주문 ID")
    void getOrderPayPrice_Fail_NotFound() {
        Long invalidOrderId = 999L;
        given(orderRepository.findByOrderIdWithAll(invalidOrderId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderPayPrice(invalidOrderId))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository).findByOrderIdWithAll(invalidOrderId);
    }

    @Test
    @DisplayName("주문 조회 성공: OrderEntity 반환")
    void getOrder_Success() {
        Long orderId = 1L;
        Order mockOrder = Order.builder().build();
        mockOrder.setOrderId(orderId);
        mockOrder.setOrderStatus(OrderStatus.PAID);

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(mockOrder));

        Order result = orderService.getOrder(orderId);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PAID);

        verify(orderRepository).findByOrderId(orderId);
    }

    @Test
    @DisplayName("주문 조회 실패: 주문이 존재하지 않음")
    void getOrder_Fail_NotFound() {
        Long orderId = 999L;

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("주문 내역을 찾을 수 없습니다.");

        verify(orderRepository).findByOrderId(orderId);
    }

    @Test
    @DisplayName("주문 목록 조회 성공: 목록 반환")
    void getOrdersByUserId_Success() {
//        Long userId = 1L;
//
//        Order mockOrder = createSafeMockOrder(10L, userId);
//
//        given(orderRepository.findAllByUser_UserId(userId)).willReturn(List.of(mockOrder));
//
//        given(reviewService.getReviewedOrderItemIdList(any())).willReturn(Collections.emptyList());
//        given(refundItemRepository.findByOrderItem_OrderItemId(any())).willReturn(Collections.emptyList());
//        given(resolver.resolve(any(), any())).willReturn(OrderItemViewStatus.UNCONFIRMED);
//
//        List<OrderPreviewResponse> result = orderService.getOrdersByUserId(userId);
//
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).orderItems().get(0).bookId()).isEqualTo(100L);
//        assertThat(result.get(0).orderItems().get(0).bookTitle()).isEqualTo("테스트 책");
//        assertThat(result.get(0).orderItems().get(0).salePrice()).isEqualTo(10000);
//        assertThat(result.get(0).userId()).isEqualTo(userId);
//
//        verify(orderRepository).findAllByUser_UserId(userId);
//        verify(reviewService).getReviewedOrderItemIdList(any());
    }

    @Test
    @DisplayName("useCouponAndDecreaseStockAndPoint - 회원: 쿠폰/포인트 사용 및 재고 차감 성공")
    void useCouponAndDecreaseStockAndPoint_Member_Success() {
        Long userId = 10L;
        Long couponId = 55L;
        int pointUsed = 1000;
        Long orderId = 1L;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "userId", userId);

        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "orderId", orderId);
        ReflectionTestUtils.setField(order, "user", user);

        List<OrderItemRequest> itemRequests = List.of(
                new OrderItemRequest(100L, 2, 20000),
                new OrderItemRequest(101L, 1, 15000)
        );

        orderService.useCouponAndDecreaseStockAndPoint(order, itemRequests, couponId, pointUsed);

        verify(couponClient, times(1)).useCoupon(eq(userId), any(CouponUseRequest.class));
        verify(pointCommandService, times(1)).use(any(PointUseRequest.class), eq(userId));
        verify(bookService, times(1)).decreaseStock(anyList());
    }

    @Test
    @DisplayName("useCouponAndDecreaseStockAndPoint - 비회원: 재고 차감만 수행 (쿠폰/포인트 로직 건너뜀)")
    void useCouponAndDecreaseStockAndPoint_Guest_Success() {
        Long orderId = 2L;
        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "orderId", orderId);
        ReflectionTestUtils.setField(order, "user", null);

        List<OrderItemRequest> itemRequests = List.of(
                new OrderItemRequest(100L, 1, 10000)
        );

        orderService.useCouponAndDecreaseStockAndPoint(order, itemRequests, 99L, 5000);

        verify(couponClient, never()).useCoupon(anyLong(), any());
        verify(pointCommandService, never()).use(any(), anyLong());
        verify(bookService, times(1)).decreaseStock(anyList());
    }

    @Test
    @DisplayName("대기중인 주문 취소 및 재고 복구 - 결제 내역 없을 때: 재고/쿠폰/포인트만 복구")
    void cancelOrderIfPending_Success() {
        Long orderId = 1L;
        Long userId = 10L;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "userId", userId);

        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "orderId", orderId);
        ReflectionTestUtils.setField(order, "user", user);

        order.setOrderStatus(OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "totalBookPrice", 10000);
        ReflectionTestUtils.setField(order, "deliveryFee", 3000);
        ReflectionTestUtils.setField(order, "packagingFee", 2000);
        ReflectionTestUtils.setField(order, "couponDiscount", 1000);
        ReflectionTestUtils.setField(order, "pointUsed", 500);

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(order));
        given(paymentService.getPaymentKey(orderId)).willThrow(new PaymentNotFoundException(orderId));

        OrderItemRequest itemRequest = new OrderItemRequest(100L, 1, 10000);
        given(orderItemService.getOrderItemRequests(order)).willReturn(List.of(itemRequest));

        orderService.cancelOrderIfPending(orderId);

        verify(rabbitTemplate, times(1)).convertAndSend(
                "coupon4.exchange",
                "coupon4.use.rollback",
                orderId
        );

        verify(pointCommandService).cancelUse(orderId, userId);

        verify(bookService).increaseStock(anyList());

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    @DisplayName("cancelOrderIfPending - 결제 내역 있을 때: 결제 취소 API 호출 및 쿠폰/포인트 롤백 포함")
    void cancelOrderIfPending_WithPaymentKey() {
        Long orderId = 1L;
        Long userId = 10L;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "userId", userId);

        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "orderId", orderId);
        ReflectionTestUtils.setField(order, "user", user);
        ReflectionTestUtils.setField(order, "totalBookPrice", 10000);
        ReflectionTestUtils.setField(order, "deliveryFee", 3000);
        ReflectionTestUtils.setField(order, "packagingFee", 2000);
        ReflectionTestUtils.setField(order, "couponDiscount", 1000);
        ReflectionTestUtils.setField(order, "pointUsed", 500);
        order.setOrderStatus(OrderStatus.PENDING);

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(order));

        PaymentCancelResponse paymentKeyResponse = new PaymentCancelResponse("payKey_123", "order_123", 13500);
        given(paymentService.getPaymentKey(orderId)).willReturn(paymentKeyResponse);

        OrderItemRequest itemRequest = new OrderItemRequest(100L, 1, 10000);
        given(orderItemService.getOrderItemRequests(order)).willReturn(List.of(itemRequest));

        orderService.cancelOrderIfPending(orderId);

        verify(paymentService, times(1)).cancelPayment("payKey_123", "결제 확정 및 처리 실패", 13500L);
        verify(rabbitTemplate, times(1)).convertAndSend("coupon4.exchange", "coupon4.use.rollback", orderId);
        verify(pointCommandService, times(1)).cancelUse(orderId, userId);
        verify(bookService, times(1)).increaseStock(anyList());

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.REFUNDED);
    }


    @Test
    @DisplayName("cancelOrder - 성공: PAID 상태 주문의 정상적인 결제 취소 및 재고/포인트 복구")
    void cancelOrder_Paid_Success() {
        Long orderId = 1L;
        Long userId = 10L;
        String cancelReason = "단순 변심";

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "userId", userId);

        Book book = Book.builder().build();
        ReflectionTestUtils.setField(book, "id", 100L);

        OrderItem item = OrderItem.builder().build();
        ReflectionTestUtils.setField(item, "book", book);
        ReflectionTestUtils.setField(item, "quantity", 1);

        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "orderId", orderId);
        ReflectionTestUtils.setField(order, "user", user);
        ReflectionTestUtils.setField(order, "orderItems", new ArrayList<>(List.of(item)));
        ReflectionTestUtils.setField(order, "pointUsed", 500);
        order.setOrderStatus(OrderStatus.PAID);

        Payment mockPayment = mock(Payment.class);
        given(mockPayment.getPaymentKey()).willReturn("payKey");
        given(mockPayment.getAmount()).willReturn(15000);

        TossPaymentResponse.Cancel mockCancel = mock(TossPaymentResponse.Cancel.class);
        given(mockCancel.cancelAmount()).willReturn(15000L);

        TossPaymentResponse mockTossResponse = mock(TossPaymentResponse.class);
        given(mockTossResponse.cancels()).willReturn(List.of(mockCancel));
        given(mockTossResponse.status()).willReturn("CANCELED");
        given(mockTossResponse.method()).willReturn("CARD");
        given(mockTossResponse.paymentKey()).willReturn("payKey");
        given(mockTossResponse.orderId()).willReturn("orderId");

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(order));
        given(paymentService.getPaymentEntity(orderId)).willReturn(mockPayment);
        given(paymentService.cancelPayment(anyString(), anyString(), anyLong())).willReturn(mockTossResponse);

        orderService.cancelOrder(orderId, cancelReason);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.REFUNDED);
        assertThat(order.getDeliveryStatus()).isEqualTo(DeliveryStatus.CANCELED);

        verify(bookService, times(1)).increaseStock(anyList());
        verify(pointCommandService, times(1)).cancelUse(orderId, userId);
        verify(paymentService, times(1)).savePayment(any());
    }

    @Test
    @DisplayName("cancelOrder - 예외 처리: 결제 취소 중 PaymentConfirmException 발생 시에도 상태 변경 및 저장 수행")
    void cancelOrder_PaymentConfirmException() {

        Long orderId = 1L;
        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "orderId", orderId);
        ReflectionTestUtils.setField(order, "orderItems", new ArrayList<>());
        ReflectionTestUtils.setField(order, "pointUsed", 0);
        order.setOrderStatus(OrderStatus.PAID);

        Payment mockPayment = mock(Payment.class);
        given(mockPayment.getPaymentKey()).willReturn("payKey");
        given(mockPayment.getAmount()).willReturn(10000);
        given(mockPayment.getPayMethod()).willReturn("CARD");
        given(mockPayment.getSendOrderId()).willReturn("orderId");

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(order));
        given(paymentService.getPaymentEntity(orderId)).willReturn(mockPayment);

        given(paymentService.cancelPayment(anyString(), anyString(), anyLong()))
                .willThrow(new PaymentConfirmException("이미 처리된 요청입니다."));

        orderService.cancelOrder(orderId, "취소 사유");

        verify(paymentService, times(1)).savePayment(any(PaymentCreateRequest.class));

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    @DisplayName("cancelOrder - PENDING 상태: 결제 취소 API 호출 없이 재고 복구 및 상태 변경")
    void cancelOrder_Pending_Success() {
        Long orderId = 1L;

        Book book = Book.builder().build();
        ReflectionTestUtils.setField(book, "id", 100L);
        OrderItem item = OrderItem.builder().build();
        ReflectionTestUtils.setField(item, "book", book);
        ReflectionTestUtils.setField(item, "quantity", 1);

        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "orderId", orderId);
        ReflectionTestUtils.setField(order, "orderItems", new ArrayList<>(List.of(item)));
        ReflectionTestUtils.setField(order, "pointUsed", 0);
        order.setOrderStatus(OrderStatus.PENDING);

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(order));

        orderService.cancelOrder(orderId, "취소");

        verify(paymentService, never()).getPaymentEntity(anyLong());
        verify(paymentService, never()).cancelPayment(anyString(), anyString(), anyLong());

        verify(bookService, times(1)).increaseStock(anyList());

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    @DisplayName("비회원 주문 조회 성공 - 주문번호와 비밀번호가 일치하면 응답을 반환한다")
    void getGuestOrderResponse_Success() {
        Long orderId = 1L;
        String password = "password123!";
        OrderTrackingRequest request = new OrderTrackingRequest(orderId, password);

        Order mockOrder = Order.builder().build();
        ReflectionTestUtils.setField(mockOrder, "orderId", orderId);
        ReflectionTestUtils.setField(mockOrder, "user", null);
        ReflectionTestUtils.setField(mockOrder, "orderPassword", password);

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(mockOrder));

        OrderResponse response = orderService.getGuestOrderResponse(request);

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("비회원 주문 조회 실패 - 회원 주문인 경우 예외가 발생한다")
    void getGuestOrderResponse_Fail_MemberOrder() {
        Long orderId = 1L;
        OrderTrackingRequest request = new OrderTrackingRequest(orderId, "anyPassword");

        User member = User.builder().build();
        ReflectionTestUtils.setField(member, "userId", 1L);
        ReflectionTestUtils.setField(member, "email", "user@test.com");

        Order mockOrder = Order.builder().build();
        ReflectionTestUtils.setField(mockOrder, "orderId", orderId);
        ReflectionTestUtils.setField(mockOrder, "user", member);

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(mockOrder));

        assertThatThrownBy(() -> orderService.getGuestOrderResponse(request))
                .isInstanceOf(OrderAccessDeniedException.class)
                .hasMessageContaining("해당 주문에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("비회원 주문 조회 실패 - 비밀번호가 틀리면 예외가 발생한다")
    void getGuestOrderResponse_Fail_InvalidPassword() {
        Long orderId = 1L;
        OrderTrackingRequest request = new OrderTrackingRequest(orderId, "wrongPassword");

        Order mockOrder = Order.builder().build();
        ReflectionTestUtils.setField(mockOrder, "orderId", orderId);
        ReflectionTestUtils.setField(mockOrder, "user", null);
        ReflectionTestUtils.setField(mockOrder, "orderPassword", "correctPassword");

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(mockOrder));

        assertThatThrownBy(() -> orderService.getGuestOrderResponse(request))
                .isInstanceOf(InvalidOrderPasswordException.class);
    }

    @Test
    @DisplayName("사용자 상태 변경 성공: 환불된 아이템(Repository 조회)은 제외하고 상태 변경")
    void changeOrderStatus_ByUser_Success_WithRefunds() {
        Long orderId = 1L;
        ConfirmStatus newStatus = ConfirmStatus.CONFIRMED;

        OrderItem item1 = mock(OrderItem.class);
        given(item1.getOrderItemId()).willReturn(10L);

        OrderItem item2 = mock(OrderItem.class);
        given(item2.getOrderItemId()).willReturn(20L);

        Order mockOrder = mock(Order.class);
        given(mockOrder.getOrderItems()).willReturn(List.of(item1, item2));
        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(mockOrder));

        RefundItem mockRefundItem = mock(RefundItem.class);

        given(mockRefundItem.getOrderItem()).willReturn(item2);

        given(refundItemRepository.findAlLByOrderItem_OrderItemIdInAndRefundItemStatus(
                anyList(), eq(RefundItemStatus.APPROVED))
        ).willReturn(List.of(mockRefundItem));

        orderService.changeOrderStatus_ByUser(orderId, newStatus);

        verify(orderItemService, times(1)).changeStatusOrderItem_byUser(10L, newStatus);
        verify(orderItemService, never()).changeStatusOrderItem_byUser(20L, newStatus);
        verify(pointCommandService).reward(mockOrder);
    }

    @Test
    @DisplayName("사용자 상태 변경 성공: 환불 내역이 없으면 모든 아이템 상태 변경")
    void changeOrderStatus_ByUser_Success_NoRefunds() {
        // Given
        Long orderId = 1L;
        ConfirmStatus newStatus = ConfirmStatus.CONFIRMED;

        OrderItem item1 = mock(OrderItem.class);
        given(item1.getOrderItemId()).willReturn(10L);
        OrderItem item2 = mock(OrderItem.class);
        given(item2.getOrderItemId()).willReturn(20L);

        Order mockOrder = mock(Order.class);
        given(mockOrder.getOrderItems()).willReturn(List.of(item1, item2));
        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(mockOrder));

        // 환불 내역이 없는 경우 (빈 리스트 반환)
        given(refundItemRepository.findAlLByOrderItem_OrderItemIdInAndRefundItemStatus(
                anyList(), eq(RefundItemStatus.APPROVED))
        ).willReturn(Collections.emptyList());

        // When
        orderService.changeOrderStatus_ByUser(orderId, newStatus);

        // Then
        // 모든 아이템에 대해 호출 확인
        verify(orderItemService).changeStatusOrderItem_byUser(10L, newStatus);
        verify(orderItemService).changeStatusOrderItem_byUser(20L, newStatus);
        verify(pointCommandService).reward(mockOrder);
    }

    @Test
    @DisplayName("상태 변경 및 포인트 적립 성공")
    void changeOrderStatus_ByUser_Success() {
        Long orderId = 1L;
        ConfirmStatus newStatus = ConfirmStatus.CONFIRMED;

        OrderItem item1 = mock(OrderItem.class);
        given(item1.getOrderItemId()).willReturn(10L);
        OrderItem item2 = mock(OrderItem.class);
        given(item2.getOrderItemId()).willReturn(20L);

        Order mockOrder = mock(Order.class);
        given(mockOrder.getOrderItems()).willReturn(List.of(item1, item2));

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(mockOrder));

        orderService.changeOrderStatus_ByUser(orderId, newStatus);

        verify(orderItemService, times(2)).changeStatusOrderItem_byUser(anyLong(), any(ConfirmStatus.class));
        verify(orderItemService).changeStatusOrderItem_byUser(10L, newStatus);
        verify(orderItemService).changeStatusOrderItem_byUser(20L, newStatus);

        verify(pointCommandService, times(1)).reward(mockOrder);

    }
}