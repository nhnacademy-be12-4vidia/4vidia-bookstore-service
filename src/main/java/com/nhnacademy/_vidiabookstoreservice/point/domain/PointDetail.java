package com.nhnacademy._vidiabookstoreservice.point.domain;

import com.nhnacademy._vidiabookstoreservice.point.domain.converters.PointReasonConverter;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reason", nullable = false)
    @Convert(converter = PointReasonConverter.class)
    private PointReason reason;


    @Column(name = "expired_at")
    private LocalDateTime expiredAt;


    public PointDetail(Long userId, Long orderId, Long pointPolicyId, int price, LocalDateTime createdDate,LocalDateTime expiredAt, PointReason reason) {
        this.userId = userId;
        this.orderId = orderId;
        this.pointPolicyId = pointPolicyId;
        this.price = price;
        this.createdAt=createdDate;
        this.expiredAt=expiredAt;
        this.reason = reason;
    }


    //1. 적립
    public static PointDetail reward(Long userId, Long orderId, int price) {
        return new PointDetail(userId, orderId, null, price, LocalDateTime.now(),LocalDateTime.now().plusYears(1), PointReason.ORDER_REWARD);
    }

    //2. 정책 적립
    public static PointDetail rewardByPolicy(Long userId, Long orderId, Long policyId, int price) {
        return new PointDetail(userId, orderId, policyId, price, LocalDateTime.now(),LocalDateTime.now().plusYears(1), PointReason.POLICY_REWARD);
    }

    //3. 포인트 사용(차감)
    public static PointDetail use(Long userId, Long orderId, int useAmount) {
        return new PointDetail(userId, orderId, null, -useAmount, LocalDateTime.now(),null, PointReason.ORDER_USE);
    }

    public void decrease(int amount){
        if(amount < 0) {
            throw new IllegalArgumentException("감소 금액은 양수여야 합니다.");
        }
        if(this.price<amount){
            throw new IllegalArgumentException("차감 금액이 적립 금액보다 큽니다.");
        }
        this.price-=amount;
    }

    //4. 환불(환급) -> 반품했을 경우 포인트로 환불, 결제취소 시 포인트 환불
    public static PointDetail refund(Long userId, Long orderId, int refundAmount) {
        return new PointDetail(userId, orderId, null, refundAmount, LocalDateTime.now(),null, PointReason.ORDER_CANCEL_REFUND);
    }

    // 5. 소멸 처리
    public void expire(){
        this.price = 0;
        this.reason = PointReason.POINT_EXPIRE;
    }






}
