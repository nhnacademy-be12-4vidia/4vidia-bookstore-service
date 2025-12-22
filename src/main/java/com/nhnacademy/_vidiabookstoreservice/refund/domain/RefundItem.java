package com.nhnacademy._vidiabookstoreservice.refund.domain;

import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.refund.domain.converter.RefundStatusConverter;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.exception.RefundPriceInvalidException;
import com.nhnacademy._vidiabookstoreservice.refund.exception.already.RefundAlreadyApprovedException;
import com.nhnacademy._vidiabookstoreservice.refund.exception.already.RefundAlreadyRejectedException;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "refund_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundItem { // 실제 반품 아이템 관리
    @Id
    @Column(name = "refund_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_id", nullable = false)
    private Refund refund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "refund_price")
    private Integer refundPrice;

    @Column(name = "refund_item_status", nullable = false)
    @Convert(converter = RefundStatusConverter.class)
    private RefundStatus refundStatus;

    @Column(name = "reject_detail")
    private String rejectDetail;

    @Builder
    public RefundItem(Refund refund, OrderItem orderItem, Integer refundPrice, RefundStatus refundStatus, String rejectDetail){
        this.refund = refund;
        this.orderItem = orderItem;
        this.refundPrice = refundPrice;
        this.refundStatus = refundStatus;
        this.rejectDetail = rejectDetail;
    }

    // 관리자 반품 승인 (update)
    public void accept() {
        if (this.refundStatus == RefundStatus.APPROVED) {
            throw new RefundAlreadyApprovedException();
        }
        this.refundStatus = RefundStatus.APPROVED;
    }

    // 관리자 반품 거절 (update)
    public void reject(String rejectDetail) {
        if(this.refundStatus == RefundStatus.REJECTED){
            throw new RefundAlreadyRejectedException();
        }
        this.refundStatus = RefundStatus.REJECTED;
        this.rejectDetail = rejectDetail;
    }

    // 반품 신청 시 초기 객체 생성
    public static RefundItem createRefundItem(OrderItem orderItem){
        return RefundItem.builder()
                .orderItem(orderItem)
                .refundStatus(RefundStatus.PROCESS)
                .build();
    }

    public void assignToRefund(Refund refund){
        this.refund = refund;
    }

    public void updateRefundPrice(Integer refundPrice){
        if(refundPrice < 0){
            throw new RefundPriceInvalidException();
        }
        this.refundPrice = refundPrice;
    }
}