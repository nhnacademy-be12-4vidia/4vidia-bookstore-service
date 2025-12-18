package com.nhnacademy._vidiabookstoreservice.refund.domain;

import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.refund.exception.RefundAlreadyAcceptException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "refund")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund {
    @Id
    @Column(name = "refund_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refund_order_item"))
    private OrderItem orderItem;

    @Column(name = "damaged", nullable = false)
    private Boolean damaged;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "description", length = 250)
    private String description; // 반품 신청 사유

    @Setter
    @Convert(converter = RefundStatusConverter.class)
    @Column(name = "refund_status", nullable = false)
    private RefundStatus refundStatus;

    @Column(name = "reject_detail")
    private String rejectDetail; // 반품 거절 사유

    @Column(name = "refund_price", nullable = false)
    private Integer refundPrice; // 환불 금액

    @Builder
    public Refund(OrderItem orderItem, boolean damaged, String description, RefundStatus refundStatus, String rejectDetail, Integer refundPrice){
        this.orderItem = orderItem;
        this.damaged = damaged;
        this.createdAt = LocalDateTime.now();
        this.description = description;
        this.refundStatus = refundStatus;
        this.rejectDetail = rejectDetail;
        this.refundPrice = (refundPrice == null) ? 0 : refundPrice;
    }

    // 관리자 반품 승인 (update)
    public void accept() {
        if (this.refundStatus == RefundStatus.ACCEPT) {
            throw new RefundAlreadyAcceptException();
        }
        this.refundStatus = RefundStatus.ACCEPT;
    }

    // 관리자 반품 거절 (update)
    public void reject(String rejectDetail) {
        this.refundStatus = RefundStatus.REJECT;
        this.rejectDetail = rejectDetail;
    }

    public static Refund reject(OrderItem item, String reason, String refundDetail) {
        return Refund.builder()
                .orderItem(item)
                .damaged(false)
                .description(reason)
                .refundStatus(RefundStatus.REJECT)
                .rejectDetail(refundDetail)
                .build();
    }

    public static Refund accept(OrderItem item, String reason, Integer refundPrice) {
        return Refund.builder()
                .orderItem(item)
                .damaged(false)
                .description(reason)
                .refundStatus(RefundStatus.ACCEPT)
                .refundPrice(refundPrice)
                .build();
    }

    public static Refund process(OrderItem item, String reason) {
        return Refund.builder()
                .orderItem(item)
                .damaged(true)
                .description(reason)
                .refundStatus(RefundStatus.PROCESS)
                .build();
    }
}
