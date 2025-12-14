package com.nhnacademy._vidiabookstoreservice.point.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.repository.PointPolicyRepository;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointPolicyRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointRefundRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointUseRequest;
import com.nhnacademy._vidiabookstoreservice.point.repository.PointDetailRepository;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.refund.repository.RefundRepository;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointCommandServiceImpl implements PointCommandService {

    private final PointDetailRepository pointDetailRepository;
    private final PointPolicyRepository pointPolicyRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;

    /**
     *  1. 주문완료로 기본 적립 (구매 확정)
     */
    @Override
    public void reward(Order order) {
        long userId = order.getUser().getUserId();

        // 중복 적립 방지
        if (pointDetailRepository.existsByUserIdAndOrderIdAndReason(userId, order.getOrderId(), PointReason.ORDER_REWARD)) {
            return;
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        int gradeRate = user.getGrade().getPointRate(); // 적립률 (%)
        int realPrice = orderRepository.calculateNetOrderPrice(order.getOrderId(), PointReason.ORDER_CANCEL_REFUND); // 실제 결제금액

        if (realPrice <= 0 || gradeRate <= 0) {
            return;
        }

        int points = BigDecimal.valueOf(realPrice)
                .multiply(BigDecimal.valueOf(gradeRate))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .intValue();

        if (points <= 0) {
            return;
        }

        PointDetail detail = PointDetail.reward(
                userId,
                order.getOrderId(),
                points
        );

        pointDetailRepository.save(detail);

        user.addPoint(points);
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
    @Override
    public void use(PointUseRequest request, Long userId) {
        int totalPrice = pointDetailRepository.getRemainPoint(userId);
        int usePrice = request.price(); // 사용자가 작성한 포인트 사용 금액

        if(usePrice > totalPrice){
            throw new IllegalArgumentException("보유포인트가 부족합니다.");
        }else if(usePrice < 0) {
            throw new IllegalArgumentException("사용금액은 음수일 수 없습니다.");
        }else if(usePrice == 0){
            return;
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

            if (available >= remainingToUse) {
                detail.decrease(remainingToUse);
                usedTotal += remainingToUse;
                remainingToUse = 0;
            } else {
                detail.decrease(available);
                usedTotal += available;
                remainingToUse -= available;
            }
            pointDetailRepository.save(detail);
        }
        // 포인트 사용 기록은 한 번만
        pointDetailRepository.save(PointDetail.use(
                userId,
                request.orderId(),
                usedTotal
        ));

        userRepository.findById(userId)
                .ifPresent(user-> user.subtractPoint(usePrice));
    }

    /**
     * 4. 결제 취소 || 결제 실패 -> 사용한 포인트 전체 환불
     */
    @Override
    public void cancelUse(Long orderId, Long userId) {

        PointDetail used = pointDetailRepository.findByOrderIdAndReason(orderId, PointReason.ORDER_USE)
                .orElseThrow(() -> new IllegalArgumentException("사용 포인트 이력이 없습니다."));

        int refundAmount = used.getPrice();

        restoreRemainingPoint(userId, refundAmount);

        // 환불 기록은 한 번만
        pointDetailRepository.save(
                PointDetail.cancelUse(userId, orderId, refundAmount)
        );

        userRepository.findById(userId)
                .ifPresent(user -> user.addPoint(refundAmount));
    }

    /* 5. 반품 */

    /**
     * 단순 변심 반품
     */
    @Override
    public void refundSimpleChange(PointRefundRequest request, Long userId) {
        int refundAmount = request.refundPoint();
        if(refundAmount <=0) return;

        restoreRemainingPoint(userId, request.refundPoint());

        // 환불 이력
        pointDetailRepository.save(
                PointDetail.refund(
                        userId,
                        request.orderId(),
                        refundAmount + request.cashPoint(),
                        request.cashPoint()
                )
        );

        userRepository.findById(userId).ifPresent(user -> user.addPoint(refundAmount + request.cashPoint()));
    }

    /**
     * 파손으로 인한 반품 시 포인트 새로운 만료일로 적립
     */
    @Override
    public void refundDamaged(PointRefundRequest request, Long userId) {
        int refundAmount = request.refundPoint();
        if(refundAmount<=0) return;

        // 정책에 따른 새로운 만료일
        LocalDate newExpiredDate = LocalDate.now().plusWeeks(1);

        pointDetailRepository.save(PointDetail.damageRefund(
                        userId,
                        request.orderId(),
                        refundAmount + request.cashPoint(),
                        newExpiredDate
                )
        );

        userRepository.findById(userId).ifPresent(user ->
                        user.addPoint(refundAmount + request.cashPoint())
                );
    }
    /**
     * 만료되지 않은 기존 적립 포인트의 remainingPrice를 복구
     */
    private void restoreRemainingPoint(Long userId, int restoreAmount) {
        int remaining = restoreAmount;
        LocalDate now = LocalDate.now();

        List<PointDetail> earnedList = pointDetailRepository.findPointForRefund(userId, now);

        for (PointDetail detail : earnedList) {
            if (remaining == 0) break;

            int canRestore = detail.getPrice() - detail.getRemainingPrice();

            int restore = Math.min(canRestore, remaining);
            detail.increase(restore);
            remaining -= restore;
        }
    }


}
