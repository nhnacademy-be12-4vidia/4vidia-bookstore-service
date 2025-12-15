package com.nhnacademy._vidiabookstoreservice.point.domain;

import com.nhnacademy._vidiabookstoreservice.admin.domain.PointPolicy;
import com.nhnacademy._vidiabookstoreservice.point.domain.converters.PointReasonConverter;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_policy_id")
    private PointPolicy pointPolicy;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reason", nullable = false)
    @Convert(converter = PointReasonConverter.class)
    private PointReason reason;

    @Column(name = "expired_date")
    private LocalDate expiredDate;

    @Setter
    @Column(name = "remaining_price", nullable = false)
    private Integer remainingPrice;

    @Builder
    public PointDetail(Long userId, Long orderId, PointPolicy pointPolicy, int price,
                       LocalDateTime createdAt,LocalDate expiredDate, PointReason reason, Integer remainingPrice) {
        this.userId = userId;
        this.orderId = orderId;
        this.pointPolicy = pointPolicy;
        this.price = price;
        this.createdAt=createdAt;
        this.expiredDate=expiredDate;
        this.reason = reason;
        this.remainingPrice = (remainingPrice == null) ? 0 : remainingPrice;
    }


    /**
     * 주문확정 버튼 -> 적립 (userId, orderId, price)
     */
    public static PointDetail reward(Long userId, Long orderId, int price) {
        LocalDateTime ldt = LocalDateTime.now();

        return PointDetail.builder()
                .userId(userId)
                .orderId(orderId)
                .price(price)
                .createdAt(ldt)
                .expiredDate(ldt.plusYears(1).toLocalDate())
                .reason(PointReason.ORDER_REWARD)
                .remainingPrice(price)
                .build();
    }

    /**
     * 정책 -> 적립 (userId, pointPolicy) - 회원가입, 리뷰, 포토리뷰
     */
    public static PointDetail rewardByPolicy(Long userId, PointPolicy pointPolicy) {
        LocalDateTime ldt = LocalDateTime.now();

        return PointDetail.builder()
                .userId(userId)
                .pointPolicy(pointPolicy)
                .price(pointPolicy.getPrice())
                .createdAt(ldt)
                .expiredDate(ldt.plusYears(1).toLocalDate())
                .reason(PointReason.POLICY_REWARD)
                .remainingPrice(pointPolicy.getPrice())
                .build();
    }


    /**
     * 포인트 사용 -> 차감 (userId, orderId, usePrice)
     */
    public static PointDetail use(Long userId, Long orderId, int price) {
        LocalDateTime ldt = LocalDateTime.now();

        return PointDetail.builder()
                .userId(userId)
                .orderId(orderId)
                .price(-price)
                .createdAt(ldt)
                .reason(PointReason.ORDER_USE)
                .build();
    }

    /**
     * 결제 취소 || 결제 실패 || 단순 변심 반품-> 포인트 환불 (remaining=0, 기존 내역 증가-유효기간 때문에)
     */
    public static PointDetail cancelUse(Long userId, Long orderId, int price){
        LocalDateTime ldt = LocalDateTime.now();

        return PointDetail.builder()
                .userId(userId)
                .orderId(orderId)
                .price(price)
                .createdAt(ldt)
                .reason(PointReason.ORDER_CANCEL_REFUND)
                .build();
    }

    /**
     * 파손 반품 -> 포인트 환불 (만료일 새로 생성)
     */
    public static PointDetail damageRefund(Long userId, Long orderId, int price, LocalDate expiredDate){
        LocalDateTime ldt = LocalDateTime.now();

        return PointDetail.builder()
                .userId(userId)
                .orderId(orderId)
                .price(price)
                .createdAt(ldt)
                .expiredDate(expiredDate)
                .reason(PointReason.ORDER_CANCEL_REFUND)
                .remainingPrice(price)
                .build();
    }

    /**
     * 반품금액 (cash) 포인트로 환불 (만료일 없음)
     */
    public static PointDetail refund(Long userId, Long orderId, int price, int remainingPrice) {
        LocalDateTime ldt = LocalDateTime.now();

        return PointDetail.builder()
                .userId(userId)
                .orderId(orderId)
                .price(price)
                .createdAt(ldt)
                .reason(PointReason.ORDER_CANCEL_REFUND)
                .remainingPrice(remainingPrice)
                .build();
    }

    /**
     * 만료일 지남 -> 소멸
     */
    public static PointDetail expired(Long userId){
        LocalDateTime ldt = LocalDateTime.now();

        return PointDetail.builder()
                .userId(userId)
                .price(0)
                .createdAt(ldt)
                .reason(PointReason.POINT_EXPIRE)
                .build();
    }

    /**
     * 포인트 차감 (remainingPrice 감소)
     */
    public void decrease(int amount){
        if(amount < 0) {
            throw new IllegalArgumentException("포인트 감소 금액은 양수여야 합니다.");
        }
        if(this.remainingPrice<amount){
            throw new IllegalArgumentException("차감 금액이 적립 금액보다 큽니다.");
        }
        this.remainingPrice-=amount;
    }

    /**
     * 포인트 환불 (remainingPrice 증가)
     */
    public void increase(int amount){
        if(amount < 0){
            throw new IllegalArgumentException("포인트 환불 금액은 양수여야 합니다.");
        }
        this.remainingPrice+=amount;
    }
}
