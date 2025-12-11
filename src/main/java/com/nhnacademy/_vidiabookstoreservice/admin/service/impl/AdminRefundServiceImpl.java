package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.response.AdminRefundListResponse;
import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.response.RefundDetailResponse;
import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.response.RefundItemDto;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminRefundService;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.dto.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminRefundServiceImpl implements AdminRefundService {
    private final RefundRepository refundRepository;

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
                .orElseThrow(() -> new IllegalArgumentException("Refund not found: " + refundId));

        // Refund가 OrderItem을 참조하므로 관련 정보 추출
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
    @Override
    public void acceptRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("Refund not found: " + refundId));

        // 비즈니스: 승인 시 RefundStatus 변경, OrderItem confirm 상태 변경 등
        refund.setRefundStatus(RefundStatus.ACCEPT);
        refundRepository.save(refund);

        // 예: 관련 orderItem confirmStatus 변경 (엔티티에 setter가 있어야 함)
        OrderItem oi = refund.getOrderItem();
        if (oi != null) {
            oi.setConfirmStatus(ConfirmStatus.REFUNDED);
        }

        // 추가: 포인트 환불, 알림 등 처리
    }

    @Override
    public void rejectRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("Refund not found: " + refundId));

        refund.setRefundStatus(RefundStatus.REJECT);
        refundRepository.save(refund);

        // 추가: 사용자 통지, 이력 기록 등
    }


}
