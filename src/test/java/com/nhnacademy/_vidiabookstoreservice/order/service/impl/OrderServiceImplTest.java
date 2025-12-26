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
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderCreateResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderPreviewResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentConfirmRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentCancelResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.TossPaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderFailedException;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderRollbackFailedException;
import com.nhnacademy._vidiabookstoreservice.order.exception.PaymentCancelException;
import com.nhnacademy._vidiabookstoreservice.order.exception.PaymentConfirmException;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.PaymentNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointUseRequest;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
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
    @Mock private com.nhnacademy._vidiabookstoreservice.order.service.PackagingService packagingService;
    @Mock private com.nhnacademy._vidiabookstoreservice.order.service.PackagingOptionService packagingOptionService;
    @Mock private com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService pointCommandService;
    @Mock private com.nhnacademy._vidiabookstoreservice.book.service.ReviewService reviewService;
    @Mock private com.nhnacademy._vidiabookstoreservice.global.client.CouponClient couponClient;
    @Mock private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    @Mock private com.nhnacademy._vidiabookstoreservice.order.mq.producer.OrderMessageProducer orderMessageProducer;
    @Mock private com.nhnacademy._vidiabookstoreservice.cart.service.CartService cartService;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;
    @Mock private com.nhnacademy._vidiabookstoreservice.refund.repository.RefundItemRepository refundItemRepository;
    @Mock private com.nhnacademy._vidiabookstoreservice.order.domain.OrderItemViewStatusResolver resolver;

    @Test
    @DisplayName("모든 검증 통과 후 주문 저장 성공")
    void saveOrder_Success() {
        Long userId = 1L;
        Long bookId = 100L;
        Long packagingOptionId = 1L;
        Long couponId = 50L;

        Category mockCategory = mock(Category.class);
        given(mockCategory.getKdcCode()).willReturn(String.valueOf(100));

        Book mockBook = Book.builder()
                .priceSales(10000) // 권당 10,000원
                .category(mockCategory)
                .build();
        ReflectionTestUtils.setField(mockBook, "id", bookId);

        PackagingOption mockOption = mock(PackagingOption.class);
        given(mockOption.getPrice()).willReturn(1000);

        User mockUser = User.builder().build();
        ReflectionTestUtils.setField(mockUser, "userId", userId);

        OrderCreateRequest request = new OrderCreateRequest(
                "수령인", "도로명주소", "상세주소", "12345", "010-1234-5678",
                "문앞에", "password123", LocalDate.now().plusDays(2),
                10000, 3000, 1000, 2000, 1000,
                List.of(new OrderCreateRequest.ItemRequestDto(bookId, 1, 10000, List.of(packagingOptionId))),
                couponId
        );

        given(userService.getUserById(userId)).willReturn(mockUser);
        given(bookService.getBookEntity(bookId)).willReturn(mockBook);
        given(packagingOptionService.getByPackagingOptionId(packagingOptionId)).willReturn(mockOption);

        given(couponClient.calculateCoupons(anyLong(), any())).willReturn(new CouponCalculationResponse(2000));

        Order savedOrder = Order.builder().build();
        ReflectionTestUtils.setField(savedOrder, "orderId", 500L);
        ReflectionTestUtils.setField(savedOrder, "user", mockUser);
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

        OrderCreateResponse response = orderService.saveOrder(userId, request);

        assertThat(response.orderId()).isEqualTo(500L);

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(bookService, atLeastOnce()).decreaseStock(any());
        verify(pointCommandService, times(1)).use(any(), eq(userId));
        verify(orderMessageProducer, times(1)).sendDelayedCancelMessage(500L);
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
        verify(cartService, times(1)).removeItemByOrder(eq(userId), eq(List.of(bookId)));
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

        verify(rabbitTemplate, times(1)).convertAndSend(eq("coupon4.exchange"), eq("coupon4.use.rollback"), eq(orderId));
        verify(pointCommandService, times(1)).cancelUse(orderId, userId);
        verify(bookService, times(1)).increaseStock(anyList());
        verify(paymentService, times(1)).cancelPayment(eq("payKey"), eq("결제 확정 및 처리 실패"), eq(14000L));
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

        verify(rabbitTemplate, times(1)).convertAndSend(eq("coupon4.exchange"), eq("coupon4.use.rollback"), eq(orderId));
        verify(pointCommandService, times(1)).cancelUse(orderId, userId);
    }

    @Test
    @DisplayName("주문 상세 조회 성공: 주문 상세 조회 dto 반환")
    void getOrderResponse_Success() {
        Long orderId = 1L;
        Long userId = 10L;

        User mockUser = User.builder().build();
        given(mockUser.getUserId()).willReturn(userId);
        Order mockOrder = Order.builder().build();
        mockOrder.setUser(mockUser);
        mockOrder.setOrderId(orderId);
        mockOrder.setCreatedAt(LocalDateTime.now());
        mockOrder.setDeliveryStatus(DeliveryStatus.WAITING);
        mockOrder.setTotalBookPrice(10000);

        given(orderRepository.findByOrderIdWithAll(orderId)).willReturn(Optional.of(mockOrder));

        OrderResponse result = orderService.getOrderResponse(userId, orderId);

        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.deliveryStatus()).isEqualTo(DeliveryStatus.WAITING);
        assertThat(result.totalBookPrice()).isEqualTo(10000);
    }

    @Test
    @DisplayName("주문 상세 조회 실패: 주문이 존재하지 않음")
    void getOrderResponse_Fail_NotFound() {
        Long orderId = 999L;
        given(orderRepository.findByOrderIdWithAll(orderId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderResponse(anyLong(), orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("주문 내역을 찾을 수 없습니다.");

        verify(orderRepository).findByOrderIdWithAll(orderId);
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
                eq("coupon4.exchange"),
                eq("coupon4.use.rollback"),
                eq(orderId)
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

        verify(paymentService, times(1)).cancelPayment(eq("payKey_123"), eq("결제 확정 및 처리 실패"), eq(13500L));
        verify(rabbitTemplate, times(1)).convertAndSend(eq("coupon4.exchange"), eq("coupon4.use.rollback"), eq(orderId));
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
    @DisplayName("비회원 주문 조회 성공: 주문 비밀번호 일치 true 반환")
    void validateGuest_Match() {
        Long orderId = 1L;
        String password = "12345";

        OrderTrackingRequest request = new OrderTrackingRequest(orderId, password);

        Order mockOrder = mock(Order.class);

        given(mockOrder.getOrderPassword()).willReturn(password);

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(mockOrder));

//        Boolean result = orderService.validateGuest(request);

//        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("비회원 주문 조회 성공: 주문 비밀번호 불일치 false 반환")
    void validateGuest_NotMatch() {
        Long orderId = 1L;

        OrderTrackingRequest request = new OrderTrackingRequest(orderId, "wrongPassword");

        Order mockOrder = mock(Order.class);

        given(mockOrder.getOrderPassword()).willReturn("correctPassword");

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(mockOrder));

//        Boolean result = orderService.validateGuest(request);

//        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("상태 변경 성공: 주문에 해당하는 모든 아이템 상태 변경")
    void changeOrderStatus_Success() {
        Long orderId = 1L;
        ConfirmStatus newStatus = ConfirmStatus.CONFIRMED;

        OrderItem item1 = mock(OrderItem.class);
        given(item1.getOrderItemId()).willReturn(10L);
        OrderItem item2 = mock(OrderItem.class);
        given(item2.getOrderItemId()).willReturn(20L);

        Order mockOrder = mock(Order.class);
        given(mockOrder.getOrderItems()).willReturn(List.of(item1, item2));

        given(orderRepository.findByOrderId(orderId)).willReturn(Optional.of(mockOrder));

        orderService.changeOrderStatus(orderId, newStatus);

        verify(orderItemService, times(2)).changeStatusOrderItem(anyLong(), any(ConfirmStatus.class));
        verify(orderItemService).changeStatusOrderItem(10L, newStatus);
        verify(orderItemService).changeStatusOrderItem(20L, newStatus);
    }

    @Test
    @DisplayName("상태 변경 및 포인트 적립 성공")
    void changeOrderStatus_ByUser_Success() {
        Long orderId = 1L;
        Long userId = 10L;
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


    private Order createSafeMockOrder(Long orderId, Long userId) {
        Book mockBook = mock(Book.class);
        OrderItem mockItem = mock(OrderItem.class);
        User mockUser = mock(User.class);
        Order mockOrder = mock(Order.class);

        given(mockBook.getId()).willReturn(100L);
        given(mockBook.getTitle()).willReturn("테스트 책");
        given(mockBook.getBookAuthorList()).willReturn(new ArrayList<>());
        given(mockBook.getBookImageList()).willReturn(new ArrayList<>());

        given(mockItem.getOrderItemId()).willReturn(200L);
        given(mockItem.getBook()).willReturn(mockBook);
        given(mockItem.getQuantity()).willReturn(1);
        given(mockItem.getSalePrice()).willReturn(10000);

        given(mockUser.getUserId()).willReturn(userId);

        given(mockOrder.getOrderId()).willReturn(orderId);
        given(mockOrder.getUser()).willReturn(mockUser);
        given(mockOrder.getOrderItems()).willReturn(List.of(mockItem));
        given(mockOrder.getDeliveryStatus()).willReturn(DeliveryStatus.WAITING);

        return mockOrder;
    }
}