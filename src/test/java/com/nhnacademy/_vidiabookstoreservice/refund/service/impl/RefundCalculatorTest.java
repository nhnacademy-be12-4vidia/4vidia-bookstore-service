package com.nhnacademy._vidiabookstoreservice.refund.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.global.client.CouponClient;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import com.nhnacademy._vidiabookstoreservice.point.repository.PointDetailRepository;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundAmount;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.UseCouponResponse;
import com.nhnacademy._vidiabookstoreservice.refund.repository.RefundItemRepository;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefundCalculatorTest {

    @Mock private OrderItemRepository orderItemRepository;
    @Mock private PointDetailRepository pointDetailRepository;
    @Mock private CouponClient couponClient;
    @Mock private RefundItemRepository refundItemRepository;

    @Mock private Order order;
    @Mock private OrderItem item;
    @Mock private Book book;
    @Mock private Category category;

    private RefundCalculator refundCalculator;

    @BeforeEach
    void setUp() {
        refundCalculator = new RefundCalculator(
                orderItemRepository,
                pointDetailRepository,
                couponClient,
                refundItemRepository
        );

        when(item.getOrderItemId()).thenReturn(100L);
        when(item.getOrder()).thenReturn(order);
        when(order.getOrderId()).thenReturn(1L);
    }

    @Nested
    @DisplayName("1. 쿠폰 미사용 환불 시나리오")
    class NoCouponRefundTest {
        @BeforeEach
        void innerSetUp() {
            when(order.getCouponDiscount()).thenReturn(0);
            when(item.getSalePrice()).thenReturn(10000);
            when(item.getQuantity()).thenReturn(1);
        }

        @Test
        @DisplayName("1-1. 비례 포인트 환불 (마지막 환불 아님)")
        void calculate_ProportionalPoint() {
            when(order.getTotalBookPrice()).thenReturn(20000);
            when(order.getPointUsed()).thenReturn(2000);

            when(orderItemRepository.findByOrder_orderId(1L)).thenReturn(List.of(item, mock(OrderItem.class)));
            when(refundItemRepository.existsByOrderItem_OrderItemId(100L)).thenReturn(false);

            RefundAmount result = refundCalculator.calculate(item, false, true);

            // 포인트: 2000 * 10000 / 20000 = 1000
            // 현금: (10000 - 배송비 3000) - 포인트 1000 = 6000
            assertEquals(1000, result.refundPoint());
            assertEquals(6000, result.refundCash());
        }

        @Test
        @DisplayName("1-2. 마지막 포인트 환불 (전액 정산)")
        void calculate_LastPointSettlement() {
            when(order.getPointUsed()).thenReturn(2000);

            when(orderItemRepository.findByOrder_orderId(1L)).thenReturn(List.of(item));
            when(refundItemRepository.existsByOrderItem_OrderItemId(100L)).thenReturn(false);
            when(pointDetailRepository.sumRefundedPoint(1L, PointReason.ORDER_CANCEL_REFUND)).thenReturn(1200);

            RefundAmount result = refundCalculator.calculate(item, true, false);

            // 2000 - 1200 = 800포인트 반환
            assertEquals(800, result.refundPoint());
        }
    }

    @Nested
    @DisplayName("2. 쿠폰 사용 환불 시나리오")
    class CouponRefundTest {
        @BeforeEach
        void innerSetUp() {
            when(order.getCouponDiscount()).thenReturn(5000);
            when(order.getTotalBookPrice()).thenReturn(30000);
            when(order.getPointUsed()).thenReturn(0);
            when(item.getSalePrice()).thenReturn(10000);
            when(item.getQuantity()).thenReturn(1);

            when(orderItemRepository.findByOrder_orderId(1L)).thenReturn(List.of(item, mock(OrderItem.class)));
            when(refundItemRepository.existsByOrderItem_OrderItemId(100L)).thenReturn(false);
        }

        @Test
        @DisplayName("2-1. ALL 쿠폰: 최소주문금액 미달 시 혜택 회수")
        void calculate_AllCoupon_Break() {
            UseCouponResponse coupon = new UseCouponResponse("ALL", null, null, 25000);
            when(couponClient.getUseCouponDetail(any())).thenReturn(coupon);
            when(refundItemRepository.sumRefundedPriceByOrder(1L)).thenReturn(0);

            RefundAmount result = refundCalculator.calculate(item, true, false);

            // 전체결제액(25000) - 남은정가(20000) = 5000
            assertEquals(5000, result.refundCash());
        }

        @Test
        @DisplayName("2-2. BOOK 쿠폰: 도서 일치 시 할인액 차감")
        void calculate_BookCoupon_Match() {
            when(item.getBook()).thenReturn(book);
            when(book.getId()).thenReturn(50L);

            UseCouponResponse coupon = new UseCouponResponse("BOOK", null, 50L, 0);
            when(couponClient.getUseCouponDetail(any())).thenReturn(coupon);

            RefundAmount result = refundCalculator.calculate(item, true, false);

            assertEquals(5000, result.refundCash());
        }
    }

    @Test
    @DisplayName("3. 예외 상황: 배송비 차감 후 금액이 음수일 때 0원 보정")
    void calculate_NegativeCorrection() {
        when(order.getCouponDiscount()).thenReturn(0);
        when(item.getSalePrice()).thenReturn(2000);
        when(item.getQuantity()).thenReturn(1);

        when(orderItemRepository.findByOrder_orderId(1L)).thenReturn(List.of(item, mock(OrderItem.class)));
        when(refundItemRepository.existsByOrderItem_OrderItemId(100L)).thenReturn(false);

        // 2000 - 3000 = -1000 -> 0원 보정 확인
        RefundAmount result = refundCalculator.calculate(item, false, true);

        assertEquals(0, result.refundCash());
        assertEquals(0, result.refundPoint());
    }
}