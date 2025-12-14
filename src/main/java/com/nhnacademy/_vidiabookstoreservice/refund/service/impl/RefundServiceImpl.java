package com.nhnacademy._vidiabookstoreservice.refund.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderItemNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderItemService;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointRefundRequest;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.dto.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundRequest;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.OrderItemResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundHistoryResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundResponse;
import com.nhnacademy._vidiabookstoreservice.refund.repository.RefundRepository;
import com.nhnacademy._vidiabookstoreservice.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * RefundServiceImpl : 반품 신청서 작성, 단순 변심 환불 처리 담당
 */
@Service
@Transactional
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final OrderItemService orderItemService;
    private final PointCommandService pointCommandService;

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
            OrderItem item = getOrderItem(itemId);

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

    private void handleSimpleChange(OrderItem item, String reason, long days) {
        // 10일 초과 → 즉시 거절
        if (days > 10) {
            refundRepository.save(Refund.reject(item, reason));
            return;
        }

        // 10일 이내 → 즉시 승인 + 포인트 환불
        refundRepository.save(Refund.accept(item, reason));
        orderItemService.changeStatusOrderItem(item.getOrderItemId(), ConfirmStatus.REFUNDED);

        // 사용한 포인트 환불 금액 계산
        Order order = item.getOrder();
        int totalUsedPoint = order.getPointUsed();
        int totalQuantity = orderItemRepository.sumOrderItemQuantity(order.getOrderId());
        int refundPoint;

        // 이미 환불 완료된 Item 수
        int refundedQuantity = refundRepository.sumRefundedQuantity(order.getOrderId(), RefundStatus.ACCEPT);
        int unitPoint = totalUsedPoint / totalQuantity;

        // 마지막 환불이면 남은 포인트 전부 반환 (호출 전 이미 상태 바뀜)
        // TODO (반품,포인트) 마지막 환불 확인이 안됨.
        if(refundedQuantity + item.getQuantity() == totalQuantity){
            refundPoint = totalUsedPoint - (unitPoint * refundedQuantity);
        }else{
            refundPoint = unitPoint * item.getQuantity();
        }

        PointRefundRequest pointRequest = new PointRefundRequest(
                order.getOrderId(),
                refundPoint,
                item.getSalePrice() * item.getQuantity() //TODO (반품, 포인트) 쿠폰 할인 추가해서 다시 작성 해야함.......
        );

        pointCommandService.refundSimpleChange(pointRequest, order.getUser().getUserId());
    }

    private void handleDamaged(OrderItem item, String reason) {
        Refund refund = Refund.process(item, reason);
        refundRepository.save(refund);
        orderItemService.changeStatusOrderItem(item.getOrderItemId(), ConfirmStatus.REFUND_REQUEST);
        //관리자가 승인 시 포인트 적립
    }

    private OrderItem getOrderItem(Long itemId) {
        return orderItemRepository.findById(itemId)
                .orElseThrow(() -> new OrderItemNotFoundException(itemId));
    }

}

