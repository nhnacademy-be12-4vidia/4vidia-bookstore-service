package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.AdminRefundListResponse;
import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.RefundDetailResponse;
import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.RefundItemDto;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminRefundService;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointRefundCommand;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundAmount;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundRejectRequest;
import com.nhnacademy._vidiabookstoreservice.refund.service.impl.RefundCalculator;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderItemService;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.exception.RefundNotFoundException;
import com.nhnacademy._vidiabookstoreservice.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AdminRefundServiceImpl : 관리자 반품 승인/거절 처리, 승인 시 환불 처리 담당
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AdminRefundServiceImpl implements AdminRefundService {
    private final RefundRepository refundRepository;
    private final PointCommandService pointService;
    private final OrderItemService orderItemService;
    private final RefundCalculator refundCalculator;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminRefundListResponse> listByRefundStatus(RefundStatus refundStatus, String keyword, Pageable pageable) {
        Page<Refund> refunds;
        if (refundStatus == null) {
            refunds = refundRepository.findAll(pageable);
        } else {
            refunds = refundRepository.findAllByRefundStatus(refundStatus, pageable);
        }
        return refunds.map(AdminRefundListResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundDetailResponse getRefundDetail(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundNotFoundException(refundId));

        OrderItem oi = refund.getOrderItem();
        Long orderId = oi.getOrder() != null ? oi.getOrder().getOrderId() : null;

        List<RefundItemDto> items = List.of(
                new RefundItemDto(
                        oi.getOrderItemId(),
                        oi.getBook().getTitle(),
                        oi.getQuantity(),
                        oi.getSalePrice()
                )
        );

        return new RefundDetailResponse(
                refund.getRefundId(),
                orderId,
                oi.getOrder() != null && oi.getOrder().getUser() != null ? oi.getOrder().getUser().getEmail() : null,
                oi.getOrder() != null && oi.getOrder().getUser() != null ? oi.getOrder().getUser().getName() : null,
                refund.getDescription(),
                refund.getCreatedAt(),
                refund.getRefundStatus().name(),
                items
        );
    }

    // 반품 승인 유스케이스 : 반품 상태 변경 + OrderItem 상태 변경 + 포인트 환불
    @Override
    public void acceptRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundNotFoundException(refundId));

        OrderItem item = refund.getOrderItem();
        orderItemService.changeStatusOrderItem(item.getOrderItemId(), ConfirmStatus.REFUNDED);

        Order order = item.getOrder();
        Long userId = order.getUser().getUserId();

        RefundAmount amount =
                refundCalculator.calculate(item, false); // 파손 → 배송비 차감 X

        pointService.refundDamaged(
                new PointRefundCommand(
                        order.getOrderId(),
                        amount.refundPoint(),
                        amount.refundCash()
                ),
                userId
        );

        refund.accept(); // 반품 상태 변경
    }

    // 반품 거절
    @Override
    public void rejectRefund(Long refundId, RefundRejectRequest rejectRequest) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundNotFoundException(refundId));

        refund.reject(rejectRequest.rejectDetail()); // TODO 반품 사유 전달받아야함
        orderItemService.changeStatusOrderItem(refund.getOrderItem().getOrderItemId(), ConfirmStatus.UNCONFIRMED);
    }

}