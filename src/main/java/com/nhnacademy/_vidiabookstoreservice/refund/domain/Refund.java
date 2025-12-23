package com.nhnacademy._vidiabookstoreservice.refund.domain;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.refund.domain.converter.RefundStatusConverter;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.exception.already.RefundAlreadyApprovedException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "refund")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund { // 반품 신청서
    @Id
    @Column(name = "refund_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "damaged", nullable = false)
    private Boolean damaged;

    @Column(name = "description")
    private String description; // 반품 신청 사유

    @Column(name = "refund_status")
    @Convert(converter = RefundStatusConverter.class)
    private RefundStatus refundStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "refund", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RefundItem> refundItems = new ArrayList<>();

    @Builder
    public Refund(Order order, boolean damaged, String description, RefundStatus refundStatus){
        this.order = order;
        this.damaged = damaged;
        this.createdAt = LocalDateTime.now();
        this.description = description;
        this.refundStatus = refundStatus;
    }

    public static Refund createRefundRequest(Order order, String reason, Boolean damaged){
        return Refund.builder()
                .order(order)
                .description(reason)
                .damaged(damaged)
                .refundStatus(RefundStatus.PROCESS)
                .build();
    }

    public void addRefundItem(RefundItem item){
        this.refundItems.add(item);
        item.assignToRefund(this);
    }

    // 단순 변심 승인
    public void accept() {
        if (this.refundStatus == RefundStatus.APPROVED) {
            throw new RefundAlreadyApprovedException();
        }
        this.refundStatus = RefundStatus.APPROVED;
    }
}
