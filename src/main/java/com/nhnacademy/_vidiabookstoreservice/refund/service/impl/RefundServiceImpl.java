package com.nhnacademy._vidiabookstoreservice.refund.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.OrderItemNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointRefundCommand;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundAmount;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundRequest;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.OrderItemResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundCountResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundHistoryGroupResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundResponse;
import com.nhnacademy._vidiabookstoreservice.refund.exception.SimpleRefundNotAvailableException;
import com.nhnacademy._vidiabookstoreservice.refund.exception.already.DamageRefundNotAvailableException;
import com.nhnacademy._vidiabookstoreservice.refund.repository.RefundRepository;
import com.nhnacademy._vidiabookstoreservice.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        LocalDate deliveryDate = order.getActualDeliveryDate(); // 실제 배송일 (반품 가능 조건 확인 위해서)

        boolean canReturnByChangeOfMind = false; // 단순 변심 반품 가능 여부, TODO 파손은?
        if (deliveryDate != null) {
            canReturnByChangeOfMind = deliveryDate.plusDays(10).isAfter(LocalDate.now());
        }

        List<OrderItem> orderItems =
                orderItemRepository.findAllByOrderIdWithRefunds(orderId);

        List<OrderItemResponse> returnableItems = orderItems.stream()
                .filter(OrderItem::isReturnable) // 반품 가능한 아이템만 가져옴
                .map(OrderItemResponse::from)
                .toList();

        return new RefundResponse(orderId, canReturnByChangeOfMind, returnableItems);
    }

    /**
     * 사용자 반품 내역 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RefundHistoryGroupResponse> getMyRefunds(Long userId, RefundStatus status, Pageable pageable) {
        Page<Refund> refunds = refundRepository.findMyRefunds(userId, status, pageable);
        return refunds.map(RefundHistoryGroupResponse::from);
    }

    /**
     * 반품 신청 메인
     */
    @Override
    public void refundRegister(RefundRequest request) {
        Order order = orderRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new OrderNotFoundException(request.orderId()));

        validateRefundPeriod(order, request.damaged());

        Refund refund = Refund.createRefundRequest(order, request.reason(), request.damaged());

        List<OrderItem> items = orderItemRepository.findAllById(request.orderItemIds());
        for (OrderItem item : items) {
            refund.addRefundItem(RefundItem.createRefundItem(item));
        }

        if (!request.damaged()) {
            handleSimpleChange(refund);
        }
        refundRepository.save(refund);
    }

    /**
     * 단순 변심 -> 환불 처리 or 거절
     */
    private void handleSimpleChange(Refund refund) {
        int totalPoint = 0; // 사용한 포인트 -> 포인트로
        int totalCash = 0; // 환불금액 - 사용한 포인트
        boolean deliveryFeeApplied = false; // 배송비 차감 여부 플래그

        for(RefundItem item : refund.getRefundItems()){
            item.accept(); // 승인

            // 아이템별 환불 금액 계산
            RefundAmount amount = refundCalculator.calculate(item.getOrderItem(), deliveryFeeApplied, true);

            if (!refund.getDamaged()) {
                deliveryFeeApplied = true;
            }

            item.updateRefundPrice(amount.refundCash() + amount.refundPoint());

            totalPoint += amount.refundPoint();
            totalCash += amount.refundCash();
        }
        refund.accept(); // refund 상태도 승인으로

        pointCommandService. refundSimpleChange(
                new PointRefundCommand(
                        refund.getOrder().getOrderId(),
                        totalPoint,
                        totalCash
                ),
                refund.getOrder().getUser().getUserId()
        );
    }

    /**
     * 단순 변심 반품 기간 방어 : 10일 (프론트에서 막아둠)
     * 파손/불량 반품 기간 방어 : 30일
     */
    private void validateRefundPeriod(Order order, boolean isDamaged) {
        long days = ChronoUnit.DAYS.between(order.getActualDeliveryDate(), LocalDate.now());
        if(!isDamaged){
            if(days > 10){
                throw new SimpleRefundNotAvailableException();
            }
        }else{
            if(days > 30){
                throw new DamageRefundNotAvailableException();
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RefundCountResponse getMyRefundCounts(Long userId) {
        List<Object[]> results = refundRepository.countByUserGroupByStatus(userId);

        long total = 0;
        long process = 0;
        long approved = 0;

        for (Object[] row : results) {
            RefundStatus status = (RefundStatus) row[0];
            long count = (long) row[1];
            total += count;
            if (status == RefundStatus.PROCESS) process = count;
            else if (status == RefundStatus.APPROVED) approved = count;
        }

        return new RefundCountResponse(total, process, approved);
    }

}