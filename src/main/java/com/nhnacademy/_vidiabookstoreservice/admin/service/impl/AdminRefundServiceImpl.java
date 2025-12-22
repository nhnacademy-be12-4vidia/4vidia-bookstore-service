package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.AdminRefundListResponse;
import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.RefundDetailResponse;
import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.RefundItemDto;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminRefundService;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointRefundCommand;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundAmount;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundItemUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.refund.exception.RefundStatusInvalidException;
import com.nhnacademy._vidiabookstoreservice.refund.repository.RefundItemRepository;
import com.nhnacademy._vidiabookstoreservice.refund.service.impl.RefundCalculator;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderItemService;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.exception.RefundNotFoundException;
import com.nhnacademy._vidiabookstoreservice.refund.repository.RefundRepository;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AdminRefundServiceImpl : 관리자 반품 승인/거절 처리, 승인 시 환불 처리 담당
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminRefundServiceImpl implements AdminRefundService {
    private final RefundRepository refundRepository;
    private final PointCommandService pointService;
    private final RefundCalculator refundCalculator;
    private final RefundItemRepository refundItemRepository;

    /**
     * 관리자 반품 리스트 조회 (반품 상태 별 조회 가능)
     */
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

    /**
     * 관리자 반품 상세내역 조회
     */
    @Override
    @Transactional(readOnly = true)
    public RefundDetailResponse getRefundDetail(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundNotFoundException(refundId));

        Order order = refund.getOrder();
        User user = order.getUser();

        //refundItemDto
        List<RefundItem> refundItems = refundItemRepository.findAllByRefund_RefundId(refundId);

        List<RefundItemDto> refundItemDtos = refundItems.stream().map(RefundItemDto::of).toList();

        return new RefundDetailResponse(
                refund.getRefundId(),
                order.getOrderId(),
                user.getEmail(),
                user.getName(),
                refund.getDescription(),
                refund.getCreatedAt(),
                refund.getRefundStatus(),
                refundItemDtos
        );
    }

    /**
     * 관리자 반품 승인/거절 요청 들어오는 서비스
     */
    @Override
    public void updateRefundStatus(Long refundItemId, RefundItemUpdateRequest request) {
        switch (request.refundStatus().name()) {
            case "APPROVED":
                acceptRefund(refundItemId);
                break;
            case "REJECTED":
                rejectRefund(refundItemId, request.rejectDetail());
                break;
            default:
                throw new RefundStatusInvalidException();
        }
    }


    /**
     *  반품 승인 유스케이스 : 반품 상태 변경 + OrderItem 상태 변경 + 포인트 환불
     */
    @Override
    public void acceptRefund(Long refundItemId) {
        log.info("관리자 반품 승인 시작 : 반품 아이템 ID={}", refundItemId);

        RefundItem refundItem = refundItemRepository.findById(refundItemId)
                .orElseThrow(() -> new RefundNotFoundException(refundItemId));

        refundItem.accept();

        OrderItem item = refundItem.getOrderItem();
        Order order = item.getOrder();

        RefundAmount amount =
                refundCalculator.calculate(item, false, false); // 파손 → 배송비 차감 X

        refundItem.updateRefundPrice(amount.refundCash() + amount.refundPoint());

        pointService.refundDamaged(
                new PointRefundCommand(
                        order.getOrderId(),
                        amount.refundPoint(),
                        amount.refundCash()
                ),
                order.getUser().getUserId()
        );

    }

    // 반품 거절
    @Override
    public void rejectRefund(Long refundItemId, String rejectDetail) {
        RefundItem refundItem = refundItemRepository.findById(refundItemId)
                .orElseThrow(() -> new RefundNotFoundException(refundItemId));

        refundItem.reject(rejectDetail);
        log.info("관리자 반품 거절 완료 : ID={}", refundItemId);
    }

}