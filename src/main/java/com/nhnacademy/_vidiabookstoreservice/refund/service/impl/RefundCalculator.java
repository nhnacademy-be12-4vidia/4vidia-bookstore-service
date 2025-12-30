package com.nhnacademy._vidiabookstoreservice.refund.service.impl;

import com.nhnacademy._vidiabookstoreservice.global.client.CouponClient;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import com.nhnacademy._vidiabookstoreservice.point.repository.PointDetailRepository;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundAmount;
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

        int refundAmount;
        if (order.getCouponDiscount() == 0) { // 쿠폰 사용 x (할인 안 받았음)
            refundAmount = item.getSalePrice() * item.getQuantity();
        } else { // 쿠폰 사용 o
            refundAmount = calculateCouponRefund(item, order);
        }

        if (subtractDeliveryFee && !isAlreadySubtracted) {
            refundAmount -= REFUND_DELIVERY_FEE;
        }

        refundAmount = Math.max(refundAmount, 0);

        boolean isLastRefund = orderItemRepository.findByOrder_orderId(order.getOrderId()).stream()
                // RefundItem이 없는(아직 환불 안된) OrderItem 필터
                .filter(orderItem -> !refundItemRepository.existsByOrderItem_OrderItemId(orderItem.getOrderItemId()))
                .count() == 1;

        int refundPoint;
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
            int denominator = order.getTotalBookPrice() - order.getCouponDiscount();
            refundPoint = (denominator > 0)
                    ? (int) ((long) order.getPointUsed() * refundAmount / denominator)
                    : 0;
        }

        int refundCash = Math.max(refundAmount - refundPoint, 0);

        return new RefundAmount(refundPoint, refundCash);
    }

    /**
     * 쿠폰 할인 금액 계산
     */
    private int calculateCouponRefund(OrderItem item, Order order) {
        UseCouponResponse coupon =
                couponClient.getUseCouponDetail(new RefundCouponRequest(order.getOrderId()));

        int refundAmount = item.getSalePrice() * item.getQuantity(); // 해당 아이템이 쿠폰 대상이 아닐 경우 기본 환불 금액

        switch (coupon.discountTargetType()) { // 쿠폰을 썼는데
            case "ALL" -> { // 모든 도서 대상 (가격 대비)
                refundAmount = calculateMinPriceCouponRefund(item, order, coupon.minOrderAmount());
            }

            case "CATEGORY" -> { // 카테고리 대상
                if (coupon.categoryKdcId() != null &&
                        coupon.categoryKdcId().equals(item.getBook().getCategory().getKdcCode())) {
                    refundAmount = calculateCategoryCouponDiscount(item, order, coupon.categoryKdcId(), coupon.minOrderAmount()); // 카테고리 쿠폰 : 무조건 최소주문금액 존재
                }
            }

            case "BOOK" -> { // 특정 도서 대상
                log.info("도서 쿠폰 사용 : 쿠폰 할인 금액 계산 시작 : 도서 아이디 : {}", coupon.bookId());
                if (coupon.bookId() != null &&
                        coupon.bookId().equals(item.getBook().getId())) { // 쿠폰 조건에 해당하는 도서면
                    refundAmount -= order.getCouponDiscount(); // 해당 도서 정가 - 할인 제외한 금액 환불
                }
            }
        }

        return Math.max(refundAmount, 0);
    }

    // 카테고리 쿠폰 할인 + 최소 주문 금액 있음
    private int calculateCategoryCouponDiscount(OrderItem item, Order order, String categoryKdcId, int minOrderAmount) {
        log.info("카테고리 쿠폰 사용 : 쿠폰 할인 금액 계산 시작 : 카테고리 아이디: {}", categoryKdcId);

        int itemPrice = item.getSalePrice() * item.getQuantity(); // 반품 신청 도서 값

        int totalCategoryPrice =
                orderItemRepository.sumCategoryItemPrice(
                        order.getOrderId(),
                        categoryKdcId
                ); // 카테고리 도서 금액 합

        int totalCategoryRefundedPrice =
                refundItemRepository.sumCategoryRefundedPriceByOrderAndCategory(
                        order.getOrderId(),
                        categoryKdcId
                );

        if(totalCategoryPrice - totalCategoryRefundedPrice - itemPrice < minOrderAmount){ // 쿠폰 깨짐
            int payPrice =
                    order.getTotalBookPrice()
                            + order.getPackagingFee()
                            - order.getCouponDiscount()
                            + order.getDeliveryFee();

            return payPrice
                    - order.getPackagingFee()
                    - (order.getTotalBookPrice() - itemPrice);
        }

        // 쿠폰 유지 → 실결제 기준 비율 환불
        int paidBookPrice = order.getTotalBookPrice() - order.getCouponDiscount();

        return (int) ((long) paidBookPrice * itemPrice / totalCategoryPrice);
    }



    /**
     *  쿠폰에 최소 주문 금액이 존재할때, 쿠폰 깨짐 여부 판단
     */
    private int calculateMinPriceCouponRefund(OrderItem item, Order order, int couponUseMinPrice) {
        log.info("ALL 쿠폰 사용 : 쿠폰 할인 금액 계산 시작 : 주문 아이템 아이디 : {}", item.getOrderItemId());

        int currentRequestPrice = item.getSalePrice() * item.getQuantity(); // 반품 신청 도서 금액
        int alreadyRefundedPrice = refundItemRepository.sumRefundedPriceByOrder(order.getOrderId()); // 이미 환불받은 가격
        int remainingBookPrice =  order.getTotalBookPrice() - alreadyRefundedPrice - currentRequestPrice; // 반품 후 남는 금액

        if (remainingBookPrice < couponUseMinPrice) {
            int totalPaid =
                    order.getTotalBookPrice()
                            + order.getPackagingFee()
                            - order.getCouponDiscount()
                            + order.getDeliveryFee();

            return totalPaid - alreadyRefundedPrice - remainingBookPrice;
        }

        // 쿠폰 유지 → 실결제 기준 비율 환불
        int paidBookPrice = order.getTotalBookPrice() - order.getCouponDiscount();
        return (int) ((long) paidBookPrice * currentRequestPrice / order.getTotalBookPrice());
    }
}

