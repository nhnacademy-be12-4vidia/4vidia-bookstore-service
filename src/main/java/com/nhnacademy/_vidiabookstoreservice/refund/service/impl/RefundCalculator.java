package com.nhnacademy._vidiabookstoreservice.refund.service.impl;

import com.nhnacademy._vidiabookstoreservice.global.client.CouponClient;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundAmount;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import com.nhnacademy._vidiabookstoreservice.point.repository.PointDetailRepository;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundCouponRequest;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.UseCouponResponse;
import com.nhnacademy._vidiabookstoreservice.refund.repository.RefundItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundCalculator {

    private final OrderItemRepository orderItemRepository;
    private final PointDetailRepository pointDetailRepository;

    private static final int REFUND_DELIVERY_FEE = 3000; // 반품 배송비
    private final CouponClient couponClient;
    private final RefundItemRepository refundItemRepository;

    public RefundAmount calculate(
            OrderItem item,
            boolean isAlreadySubtracted,
            boolean subtractDeliveryFee // 반품 배송비 여부
    ) {
        log.info("환불 금액 계산 시작, 주문 아이템 아이디 : {}", item.getOrderItemId());
        Order order = item.getOrder();

        int itemRefundPrice = item.getSalePrice() * item.getQuantity(); // 기본 환불 금액

        if (order.getCouponDiscount() > 0) { // 쿠폰 사용
            itemRefundPrice = calculateCouponRefund(item, order, itemRefundPrice);
        }

        int refundPoint;
        // 마지막 반품 여부 판단
        boolean isLastRefund = orderItemRepository.findByOrder_orderId(order.getOrderId()).stream()
                // RefundItem이 없는(아직 환불 안된) OrderItem 필터
                .filter(orderItem -> !refundItemRepository.existsByOrderItem_OrderItemId(orderItem.getOrderItemId()))
                .count() == 1;

        // 마지막 반품 : 남은 포인트 모두 반환
        if (isLastRefund) {
            log.info("마지막 반품 : 남은 포인트 모두 반환, 주문아이디 : {}", order.getOrderId());
            int alreadyRefundedPoint =
                    pointDetailRepository.sumRefundedPoint(
                            order.getOrderId(),
                            PointReason.ORDER_CANCEL_REFUND
                    );
            refundPoint = Math.max(order.getPointUsed() - alreadyRefundedPoint, 0);
        } else {
            int paidBookPrice = order.getTotalBookPrice() - order.getCouponDiscount();
            // 공식 : 전체 포인트 사용액 * (현재 상품 도서금액 / 전체 상품 결제금액)
            refundPoint = (paidBookPrice > 0)
                    ? (int) ((long) order.getPointUsed() * itemRefundPrice / paidBookPrice)
                    : 0;
        }

        if (subtractDeliveryFee && !isAlreadySubtracted) { // 배송비 차감
            itemRefundPrice -= REFUND_DELIVERY_FEE;
        }

        itemRefundPrice = Math.max(itemRefundPrice - refundPoint, 0);

        return new RefundAmount(refundPoint, itemRefundPrice);
    }

    /**
     * 쿠폰 할인 금액 계산
     * 1. ALL : 모든 도서 대상 (최소 주문 금액)
     * 2. CATEGORY : 카테고리 대상
     * 3. BOOK : 특정 도서 대상
     */
    private int calculateCouponRefund(OrderItem item, Order order, int itemPrice) {
        UseCouponResponse coupon =
                couponClient.getUseCouponDetail(new RefundCouponRequest(order.getOrderId()));

        return switch (coupon.discountTargetType()) {

            case "ALL" -> calculateAllCouponRefund(order, itemPrice, coupon.minOrderAmount());

            case "CATEGORY" -> {
                if (coupon.categoryKdcId() != null &&
                        coupon.categoryKdcId().equals(item.getBook().getCategory().getKdcCode())) {
                    yield calculateCategoryCouponRefund(order, itemPrice, coupon);
                }
                yield itemPrice;
            }

            case "BOOK" -> {
                if (coupon.bookId() != null &&
                        coupon.bookId().equals(item.getBook().getId())) {
                    yield Math.max(itemPrice - order.getCouponDiscount(), 0);
                }
                yield itemPrice;
            }

            default -> itemPrice;
        };
    }

    /**
     * ALL 쿠폰 (최소 주문 금액)
     */
    private int calculateAllCouponRefund(
            Order order,
            int itemPrice,
            int minOrderAmount
    ) {
        int alreadyRefundedPrice =
                refundItemRepository.sumRefundedPriceByOrder(order.getOrderId());

        int remainingBookPrice =
                order.getTotalBookPrice() - alreadyRefundedPrice - itemPrice;

        if (remainingBookPrice < minOrderAmount) {
            return calculateRefundWhenCouponBreaks(order, itemPrice);
        }

        int paidBookPrice = order.getTotalBookPrice() - order.getCouponDiscount();
        return (int) ((long) paidBookPrice * itemPrice / order.getTotalBookPrice());
    }

    /**
     * 카테고리 쿠폰 할인 + 최소 주문 금액 있음
     */
    private int calculateCategoryCouponRefund(
            Order order,
            int itemPrice,
            UseCouponResponse coupon
    ) {
        // 해당 카테고리의 전체 원가 합계
        int totalCategoryPrice =
                orderItemRepository.sumCategoryItemPrice(
                        order.getOrderId(),
                        coupon.categoryKdcId()
                );
        // 해당 카테고리에서 이미 환불된 원가
        int refundedCategoryPrice =
                refundItemRepository.sumCategoryRefundedPriceByOrderAndCategory(
                        order.getOrderId(),
                        coupon.categoryKdcId()
                );

        int remainingCategoryPrice = totalCategoryPrice - refundedCategoryPrice - itemPrice;

        if (remainingCategoryPrice < coupon.minOrderAmount()) {
            return calculateRefundWhenCouponBreaks(order, itemPrice);
        }

        int paidBookPrice = order.getTotalBookPrice() - order.getCouponDiscount();
        return (int) ((long) paidBookPrice * itemPrice / totalCategoryPrice);
    }

    /**
     * 쿠폰 조건 미달 시 환불 금액 계산 (혜택 회수 로직)
     */
    private int calculateRefundWhenCouponBreaks(Order order, int itemPrice) {
        int totalPaidAmount = order.getTotalBookPrice() - order.getCouponDiscount();
        int alreadyRefundedPrice = refundItemRepository.sumRefundedPriceByOrder(order.getOrderId());

        // 환불 후 남은 전체 상품의 정가 합
        int remainingTotalBookPrice = order.getTotalBookPrice() - alreadyRefundedPrice - itemPrice;

        return Math.max(totalPaidAmount - remainingTotalBookPrice, 0);
    }
}

