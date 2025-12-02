package com.nhnacademy._vidiabookstoreservice.point.domain;

import com.nhnacademy._vidiabookstoreservice.point.domain.converters.PointReasonConverter;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "point_detail")
@NoArgsConstructor

public class PointDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_detail_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "point_policy_id")
    private Long pointPolicyId; // 객체 말고 ID만 저장

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "reason", nullable = false)
    @Convert(converter = PointReasonConverter.class)
    private PointReason reason;

    public PointDetail(Long userId, Long orderId, Long pointPolicyId, int price, LocalDate createdDate, PointReason reason) {
        this.userId = userId;
        this.orderId = orderId;
        this.pointPolicyId = pointPolicyId;
        this.price = price;
        this.createdDate=createdDate;
        this.reason = reason;
    }

    public static PointDetail reward(Long userId, Long orderId, int price) {
        return new PointDetail(userId, orderId, null, price, LocalDate.now(), PointReason.ORDER_REWARD);
    }

    public static PointDetail rewardByPolicy(Long userId, Long orderId, Long policyId, int price) {
        return new PointDetail(userId, orderId, policyId, price, LocalDate.now(), PointReason.POLICY_REWARD);
    }

    public static PointDetail use(Long userId, Long orderId, int useAmount) {
        return new PointDetail(userId, orderId, null, -useAmount, LocalDate.now(), PointReason.ORDER_USE);
    }

    public static PointDetail refund(Long userId, Long orderId, int refundAmount) {
        return new PointDetail(userId, orderId, null, refundAmount, LocalDate.now(), PointReason.ORDER_CANCEL_REFUND);
    }





}
