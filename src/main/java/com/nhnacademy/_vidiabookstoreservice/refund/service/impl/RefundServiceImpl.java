package com.nhnacademy._vidiabookstoreservice.refund.service.impl;

import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundAmount;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.OrderItemNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderItemService;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointRefundCommand;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundRequest;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.OrderItemResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundHistoryResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundResponse;
import com.nhnacademy._vidiabookstoreservice.refund.repository.RefundRepository;
import com.nhnacademy._vidiabookstoreservice.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * RefundServiceImpl : 반품 신청서 작성, 단순 변심 환불 처리 담당
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final OrderItemService orderItemService;
    private final PointCommandService pointCommandService;
    private final RefundCalculator refundCalculator;


    /**
     * 반품 가능 리스트 조회
     */
    @Override
    @Transactional(readOnly = true)
    public RefundResponse getRefundList(long orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        List<OrderItem> orderItems =
                orderItemRepository.findAllByConfirmStatusAndOrder(
                        ConfirmStatus.UNCONFIRMED, order);

        return new RefundResponse(
                orderId,
                orderItems.stream().map(OrderItemResponse::from).toList()
        );
    }

    /**
     * 반품 신청
     */
    @Override
    public void refundRegister(RefundRequest request) {
        Order order = orderRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new OrderNotFoundException(request.orderId()));

        boolean damaged = request.damaged();
        long days = ChronoUnit.DAYS.between(order.getActualDeliveryDate(), LocalDate.now());

        for (Long itemId : request.orderItemIds()) {
            OrderItem item = orderItemRepository.findById(itemId)
                    .orElseThrow(() -> new OrderItemNotFoundException(itemId));;

            if (!damaged) { // 단순 변심
                handleSimpleChange(item, request.reason(), days);
            }else{
                handleDamaged(item, request.reason());
            }
        }
    }

    /**
     * 사용자 반품 내역 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<RefundHistoryResponse> getMyRefunds(Long userId, RefundStatus status) {

        List<Refund> refunds =
                status == null
                        ? refundRepository.findAllByUserId(userId)
                        : refundRepository.findAllByUserIdAndRefundStatus(userId, status);

        return refunds.stream()
                .map(RefundHistoryResponse::from)
                .toList();
    }

    /**
     * 단순 변심 -> 환불 처리 or 거절
     */
    private void handleSimpleChange(OrderItem item, String reason, long days) {
        if (days > 10) {
            refundRepository.save(
                    Refund.reject(item, reason, "출고일로부터 10일이 지나 반품 불가합니다.")
            );
            return;
        }
        //승인
        RefundAmount amount =
                refundCalculator.calculate(item, true); // 배송비 차감

        Order order = item.getOrder();

        // 포인트 내역 입력
        pointCommandService.refundSimpleChange(
                new PointRefundCommand(
                        order.getOrderId(),
                        amount.refundPoint(),
                        amount.refundCash()
                ),
                order.getUser().getUserId()
        );

        // 반품 승인 기록
        Refund refund = Refund.accept(item, reason, amount.refundCash() + amount.refundPoint());
        refundRepository.save(refund);

        orderItemService.changeStatusOrderItem(
                item.getOrderItemId(),
                ConfirmStatus.REFUNDED
        );
    }


    /*private void handleSimpleChange(OrderItem item, String reason, long days) {
        // 10일 초과 → 즉시 거절
        if (days > 10) {
            refundRepository.save(
                    Refund.reject(item, reason, "출고일로부터 10일이 지나 반품 불가합니다.")
            );
            log.info("배송일로부터 10일이 지나 반품 실패");
            return;
        }
        // 10일 이내 → 즉시 승인 + 포인트 환불
        int refundAmount = 0; // 환불 금액
        Order order = item.getOrder();

        if(order.getCouponDiscount() == 0){ //쿠폰 안 씀
            refundAmount = item.getSalePrice() * item.getQuantity();
        } else {
            refundAmount = calculateCouponRefund(item,order);
        }

        refundAmount -= REFUND_DELIVERY_FEE; // 단순 변심 반품 배송비 3000원
        if(refundAmount < 0) {
            refundAmount = 0; // 실제 배송이 이루어지지 않기 때문에 환불금액액이 음수이면 0으로 설정
        }

        // 마지막 환불 체크
        boolean isLastRefund = orderItemRepository.countNotRefundedItems(order.getOrderId(), ConfirmStatus.REFUNDED) == 1;
        int refundPoint;
        int refundCash;
        if (isLastRefund) {
            // 남은 포인트 전부 반환
            int alreadyRefundedPoint = pointDetailRepository.sumRefundedPoint(order.getOrderId(), PointReason.ORDER_CANCEL_REFUND);
            refundPoint = Math.max(order.getPointUsed() - alreadyRefundedPoint,0);
        } else {
            int denominator = order.getTotalBookPrice() - order.getCouponDiscount();
            refundPoint = (denominator > 0) ? (int) ((long) order.getPointUsed() * refundAmount / denominator) : 0;
        }
        refundCash = Math.max(refundAmount - refundPoint, 0);


        PointRefundCommand pointRequest = new PointRefundCommand(
                order.getOrderId(),
                refundPoint,
                refundCash
        );
        pointCommandService.refundSimpleChange(pointRequest, order.getUser().getUserId()); // 포인트 내역에 환불 기록
        orderItemService.changeStatusOrderItem(item.getOrderItemId(), ConfirmStatus.REFUNDED); // 주문 도서 상태 바꾸기
    }

    private int calculateCouponRefund(OrderItem item, Order order) {
        UseCouponResponse useCouponResponse = null; // TODO 실제 response 가져오기 (호출하면 안된다는데)
        if (useCouponResponse == null) {
            return item.getSalePrice() * item.getQuantity(); // 쿠폰 없음 처리
        }

        String categoryKdcId = useCouponResponse.categoryKdcId();
        Long bookId = useCouponResponse.bookId();
        int couponUseMinPrice = useCouponResponse.minOrderAmount();

        int refundAmount = item.getSalePrice() * item.getQuantity();

        if (categoryKdcId != null && item.getBook().getCategory().getKdcCode().equals(categoryKdcId)) {
            refundAmount -= order.getCouponDiscount();
        } else if (bookId != null && item.getBook().getId().equals(bookId)) {
            refundAmount -= order.getCouponDiscount();
        } else if (couponUseMinPrice > 0) {
            refundAmount = calculateMinPriceCouponRefund(item, order, couponUseMinPrice);
        }

        return refundAmount;
    }

    private int calculateMinPriceCouponRefund(OrderItem item, Order order, int couponUseMinPrice) {
        int payPrice = order.getTotalBookPrice() + order.getPackagingFee() - order.getCouponDiscount() + order.getDeliveryFee();
        int refundedPrice = orderItemRepository.sumOrderItemRefunded(order.getOrderId(), ConfirmStatus.REFUNDED);

        boolean couponBroken = (order.getTotalBookPrice() - (item.getSalePrice() * item.getQuantity()) - refundedPrice) < couponUseMinPrice;

        if (couponBroken) {
            return payPrice - order.getPackagingFee() - (order.getTotalBookPrice() - item.getSalePrice() * item.getQuantity());
        } else {
            return ((payPrice - order.getPackagingFee()) * item.getSalePrice() * item.getQuantity()) / order.getTotalBookPrice();
        }
    }
    */

    /**
     *  파손 반품 처리
     */
    private void handleDamaged(OrderItem item, String reason) {
        Refund refund = Refund.process(item, reason);
        refundRepository.save(refund);
        orderItemService.changeStatusOrderItem(item.getOrderItemId(), ConfirmStatus.REFUND_REQUEST);
        //관리자가 승인 시 포인트 적립 (AdminRefundServiceImpl : acceptRefund)
    }

}