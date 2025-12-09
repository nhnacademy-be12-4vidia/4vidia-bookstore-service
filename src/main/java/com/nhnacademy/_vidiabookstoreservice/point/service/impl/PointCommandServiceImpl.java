package com.nhnacademy._vidiabookstoreservice.point.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.repository.PointPolicyRepository;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointPolicyRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointRefundRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointOrderRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointUseRequest;
import com.nhnacademy._vidiabookstoreservice.point.repository.PointDetailRepository;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PointCommandServiceImpl implements PointCommandService {

    private final PointDetailRepository pointDetailRepository;
    private final PointPolicyRepository pointPolicyRepository;
    private final UserRepository userRepository;

    /**
     *  1. 주문완료로 기본 적립
     */
    @Override
    public void reward(PointOrderRewardRequest request, Long userId) {
        // 중복 적립 방지
        if (pointDetailRepository.existsByUserIdAndOrderIdAndReason(userId, request.orderId(), PointReason.ORDER_REWARD)) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        int gradeRate = user.getGrade().getPointRate(); // 적립률 (%)
        int price = request.price(); // 결제 금액

        if (price <= 0 || gradeRate <= 0) {
            return;
        }

        int points = BigDecimal.valueOf(price)
                .multiply(BigDecimal.valueOf(gradeRate))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .intValue();

        if (points <= 0) {
            return; // 반올림 후 0이면 적립할 필요 없음
        }

        PointDetail detail = PointDetail.reward(
                userId,
                request.orderId(),
                points
        );

        pointDetailRepository.save(detail);

        user.addPoint(points);
        userRepository.save(user); // 만약 User 엔티티가 managed 상태라면 save() 생략 가능
    }


    /**
     * 2. 포인트 정책에 따른 적립
     */
    @Override
    public void rewardByPolicy(PointPolicyRewardRequest request) {
        PointDetail detail = PointDetail.rewardByPolicy(
                request.userId(),
                pointPolicyRepository.findByPointPolicyId((request.policyId()))
        );
        pointDetailRepository.save(detail);
        userRepository.findById(request.userId())
                .ifPresent(user -> user.addPoint(detail.getPrice()));
    }


    /**
     * 3. 주문 시 포인트 사용 (차감)
     */
    @Transactional
    @Override
    public void use(PointUseRequest request, Long userId) {
        int totalPrice = pointDetailRepository.getRemainPoint(userId);
        int usePrice = request.price(); // 사용자가 작성한 포인트 사용 금액

        if(usePrice > totalPrice){
            throw new IllegalArgumentException("보유포인트가 부족합니다.");
        }else if(usePrice < 0) {
            throw new IllegalArgumentException("사용금액은 음수일 수 없습니다.");
        }

        int remainingToUse = usePrice;
        int usedTotal = 0;

        LocalDate now = LocalDate.now();
        List<PointDetail> avaiableList =
                pointDetailRepository.findAvailablePointForUse(userId, now);

        for(PointDetail detail : avaiableList){
            if(remainingToUse == 0) {
                break;
            }

            int available = detail.getRemainingPrice();

            if(available >= usePrice){ // 현 적립 포인트에서 모두 차감 가능
                detail.decrease(usePrice);
                usedTotal += remainingToUse;
                remainingToUse = 0;
            } else{
                detail.decrease(available);
                usedTotal += available;
                remainingToUse -= available;
            }
            pointDetailRepository.save(detail);
        }
        // 차감 기록은 한 번만
        pointDetailRepository.save(PointDetail.use(
                userId,
                request.orderId(),
                usedTotal
        ));

        userRepository.findById(userId)
                .ifPresent(user-> user.subtractPoint(usePrice));
    }

    /**
     * 4. 결제 취소 || 결제 실패 || 반품 -> 포인트 환불 (적립)
     */
    @Override
    public void cancelUse(Long orderId, Long userId){
        PointDetail pointDetail = pointDetailRepository.findByOrderIdAndReason(orderId, PointReason.ORDER_USE);

        pointDetailRepository.save(PointDetail.cancelUse(
                userId,
                orderId,
                pointDetail.getPrice(),
                pointDetail.getExpiredDate(),
                pointDetail.getPrice()
        ));

        userRepository.findById(userId)
                .ifPresent(user -> user.addPoint(pointDetail.getPrice()));
    }

    /**
     * 4. 반품 시 환불 (적립)
     */
    @Override
    public void refund(PointRefundRequest request, Long userId) {
        boolean alreadyRefund = pointDetailRepository
                .existsByUserIdAndOrderIdAndReason(userId,request.orderId(), PointReason.ORDER_CANCEL_REFUND);

        if(alreadyRefund) {
            throw new IllegalArgumentException("이미 환불 처리된 주문입니다.");
        }
        int refundAmount = request.amount();
        if(refundAmount <= 0) {
            throw new IllegalArgumentException("환불 금액은 0보다 커야 합니다.");
        }
        pointDetailRepository.save(PointDetail.refund(
                userId,
                request.orderId(),
                refundAmount
        ));

        userRepository.findById(userId)
                .ifPresent(user-> user.addPoint(request.amount()));
    }

}
