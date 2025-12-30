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
import com.nhnacademy._vidiabookstoreservice.refund.exception.RefundNotAvailableException;
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

        LocalDate deliveryDate = order.getActualDeliveryDate();

        boolean canReturnByChangeOfMind = false;
        if (deliveryDate != null) {
            canReturnByChangeOfMind = deliveryDate.plusDays(10).isAfter(LocalDate.now());
        }

        List<OrderItem> orderItems =
                orderItemRepository.findAllByOrderIdWithRefunds(orderId);

        List<OrderItemResponse> returnableItems = orderItems.stream()
                .filter(OrderItem::isReturnable)
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

        Page<Refund> refunds = refundRepository.findAllByUserIdAndStatusWithDetails(userId, status, pageable);

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

        // 반품 신청서 생성
        Refund refund = Refund.createRefundRequest(order, request.reason(), request.damaged());

        for (Long itemId : request.orderItemIds()) {
            OrderItem orderItem = orderItemRepository.findById(itemId)
                    .orElseThrow(() -> new OrderItemNotFoundException(itemId));

            RefundItem refundItem = RefundItem.createRefundItem(orderItem);
            refund.addRefundItem(refundItem);
        }

        if (request.damaged()) {
            log.info("파손 반품 접수 : Refund ID: {}", refund.getRefundId());
        }else{
            handleSimpleChange(refund);
        }
        refundRepository.save(refund); // 신청서 저장
    }

    /**
     * 단순 변심 -> 환불 처리 or 거절
     */
    private void handleSimpleChange(Refund refund) {
        int totalPoint = 0;
        int totalCash = 0;
        boolean deliveryFeeApplied = false; // 배송비 차감 여부 플래그

        for(RefundItem item : refund.getRefundItems()){
            item.accept();

            // 아이템별 환불 금액 계산
            RefundAmount amount = refundCalculator.calculate(item.getOrderItem(), true, deliveryFeeApplied);

            if (!refund.getDamaged()) {
                deliveryFeeApplied = true;
            }

            item.updateRefundPrice(amount.refundCash() + amount.refundPoint());

            // TODO 단순변심은 한 번에 환불처리.
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

    private void validateRefundPeriod(Order order, boolean isDamaged) {
        // TODO 단순변심 10일 지나면 프론트에서 막아놓을 예정.
        if(!isDamaged){
            long days = ChronoUnit.DAYS.between(order.getActualDeliveryDate(), LocalDate.now());
            if(days > 10){
                throw new RefundNotAvailableException();
            }
        }
    }
    @Override
    @Transactional(readOnly = true)
    public RefundCountResponse getMyRefundCounts(Long userId) {
        long total = refundRepository.countByOrder_User_UserId(userId);
        long process = refundRepository.countByOrder_User_UserIdAndRefundStatus(userId, RefundStatus.PROCESS);
        long approved = refundRepository.countByOrder_User_UserIdAndRefundStatus(userId, RefundStatus.APPROVED);

        return new RefundCountResponse(total, process, approved);
    }

}