package com.nhnacademy._vidiabookstoreservice.refund.domain;

import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.refund.dto.RefundStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
    private boolean damaged;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "description", length = 250)
    private String description;

    @Setter
    @Convert(converter = RefundStatusConverter.class)
    @Column(name = "refund_status", nullable = false)
    private RefundStatus refundStatus;

    @Builder
    public Refund(OrderItem orderItem, boolean damaged, String description, RefundStatus refundStatus){
        this.orderItem = orderItem;
        this.damaged = damaged;
        this.createdAt = LocalDateTime.now();
        this.description = description;
        this.refundStatus = refundStatus;
    }
}
