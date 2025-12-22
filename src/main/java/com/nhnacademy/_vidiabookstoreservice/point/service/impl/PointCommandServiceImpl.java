package com.nhnacademy._vidiabookstoreservice.point.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.domain.PointPolicy;
import com.nhnacademy._vidiabookstoreservice.admin.exception.PointPolicyNotFoundException;
import com.nhnacademy._vidiabookstoreservice.admin.repository.PointPolicyRepository;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointPolicyRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointRefundCommand;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointUseRequest;
import com.nhnacademy._vidiabookstoreservice.point.exception.invalid.PointInvalidException;
import com.nhnacademy._vidiabookstoreservice.point.exception.already.PointCancelAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.refund.exception.RefundPriceInvalidException;
import com.nhnacademy._vidiabookstoreservice.point.exception.invalid.PointNotEnoughException;
import com.nhnacademy._vidiabookstoreservice.point.exception.invalid.PointUseUnexpireException;
import com.nhnacademy._vidiabookstoreservice.point.exception.notfound.PointNotFoundException;
import com.nhnacademy._vidiabookstoreservice.point.exception.already.PointRewardAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.point.repository.PointDetailRepository;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.exception.invalid.GradeRateInvalidException;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PointCommandServiceImpl implements PointCommandService {

    private final PointDetailRepository pointDetailRepository;
    private final PointPolicyRepository pointPolicyRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;

    /**
     *  1. 주문완료로 기본 적립 (구매 확정)
     */
    @Override
    public void reward(Order order) {
        long userId = order.getUser().getUserId();

        // 중복 적립 방지
        if (pointDetailRepository.existsByUserIdAndOrderIdAndReason(userId, order.getOrderId(), PointReason.ORDER_REWARD)) {
            throw new PointRewardAlreadyExistsException(order.getOrderId());
        }

        User user = userService.getUserById(userId);

        int gradeRate = user.getGrade().getPointRate(); // 적립률 (%)
        int realPrice = orderRepository.calculateNetOrderPrice(order.getOrderId(), PointReason.ORDER_CANCEL_REFUND); // 순수 주문금액

        if(realPrice < 0){
            log.error("순수주문금액은 음수일 수 없음.");
            // TODO 이건 POINT 오류가 아닌 듯 한데
            throw new PointInvalidException();
        }else if(realPrice == 0){
            return; // 0이면 굳이 적립할 필요도 없음
        }

        if(gradeRate < 0){
            throw new GradeRateInvalidException();
        }

        int points = BigDecimal.valueOf(realPrice)
                .multiply(BigDecimal.valueOf(gradeRate))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .intValue();

        if (points < 0) {
            throw new PointInvalidException();
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
        PointPolicy pointPolicy = pointPolicyRepository.findByPointPolicyId(request.policyId())
                .orElseThrow(() -> new PointPolicyNotFoundException(request.policyId()));

        PointDetail detail = PointDetail.rewardByPolicy(
                request.userId(),
                pointPolicy
        );

        pointDetailRepository.save(detail);

        User user = userService.getUserById(request.userId());

        user.addPoint(detail.getPrice());
    }

    /**
     * 스케줄러 실패 / 지연으로 만료됐지만 남아있는 포인트를 즉시 정리
     */
    @Override
    public void expireIfNeeded(Long userId) {

        List<PointDetail> expiredFailPoints =
                pointDetailRepository.findExpiredPointsByUser(
                        userId,
                        LocalDate.now(),
                        PointReason.POINT_EXPIRE
                );

        if (expiredFailPoints.isEmpty()) {
            return;
        }

        for (PointDetail point : expiredFailPoints) {
            try {
                expirePoints(point);
            } catch (Exception e) {
                log.error(
                        "[포인트 보정 실패] userId={}, pointDetailId={}",
                        userId,
                        point.getId(),
                        e
                );
            }
        }
    }


    /**
     * 포인트 사용 가능 검증 + 최신화
     */
    @Override
    public void validateUsablePoint(Long userId, int pointUsed){
        if(pointUsed < 0){
            throw new PointInvalidException();
        }

        if(pointUsed == 0) return;

        int remainPoint = pointDetailRepository.getRemainPoint(userId, LocalDate.now());

        if(pointUsed > remainPoint){
            throw new PointUseUnexpireException();
        }
    }

    /**
     * 3. 주문 시 포인트 사용 (차감) - 검증 후 사용 가능할 때 도달
     */
    @Override
    public void use(PointUseRequest request, Long userId) {
        int totalPrice = pointDetailRepository.getRemainPoint(userId, LocalDate.now());
        int usePrice = request.price(); // 사용자가 작성한 포인트 사용 금액

        if(totalPrice < 0){
            throw new PointInvalidException();
        }

        if(usePrice == 0){
           return;
        }else if(usePrice < 0) {
            throw new PointInvalidException();
        }else if(usePrice > totalPrice){
            throw new PointNotEnoughException();
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

        // 유저 포인트 차감
        User user = userService.getUserById(userId);
        user.subtractPoint(usePrice);
    }

    /**
     * 4. 결제 취소 || 결제 실패 -> 사용한 포인트 전체 환불
     */
    @Override
    public void cancelUse(Long orderId, Long userId) {
        PointDetail used = pointDetailRepository.findByOrderIdAndReason(orderId, PointReason.ORDER_USE)
                .orElseThrow(() -> new PointNotFoundException(orderId));

        // 이미 취소 처리 + 환불인 경우
        if(pointDetailRepository.existsByUserIdAndOrderIdAndReason(userId, orderId, PointReason.ORDER_CANCEL_REFUND)){
            throw new PointCancelAlreadyExistsException(orderId);
        }

        int refundAmount = -used.getPrice();

        restoreRemainingPoint(userId, refundAmount);

        // 환불 기록은 한 번만
        pointDetailRepository.save(
                PointDetail.cancelUse(userId, orderId, refundAmount)
        );

        User user = userService.getUserById(userId);
        user.addPoint(refundAmount);
    }

    /* 5. 반품 */

    /**
     * 단순 변심 반품
     */
    @Override
    public void refundSimpleChange(PointRefundCommand request, Long userId) {
        int refundAmount = request.refundPoint();
        if(refundAmount < 0) {
            throw new RefundPriceInvalidException();
        }

        // 사용 가능한 포인트 복구 ( remainingPrice 업데이트 )
        restoreRemainingPoint(userId, request.refundPoint());

        // 포인트 내역 등록
        pointDetailRepository.save(
                PointDetail.refund(
                        userId,
                        request.orderId(),
                        refundAmount + request.cashPoint(),
                        request.cashPoint()
                )
        );

        User user = userService.getUserById(userId);
        user.addPoint(refundAmount + request.cashPoint());
    }

    /**
     * 파손으로 인한 반품 시 포인트 새로운 만료일로 적립
     */
    @Override
    public void refundDamaged(PointRefundCommand request, Long userId) {
        int refundAmount = request.refundPoint();
        if(refundAmount<0) {
            throw new RefundPriceInvalidException();
        }

        // 정책에 따른 새로운 만료일
        LocalDate newExpiredDate = LocalDate.now().plusWeeks(1);

        /**
         * cash 만료일 없이 환불
         */
        pointDetailRepository.save(PointDetail.refund(
                        userId,
                        request.orderId(),
                        request.cashPoint(),
                        request.cashPoint()
                )
        );

        /**
         * 사용한 포인트 새로운 만료일 생성해서 환불
         */
        pointDetailRepository.save(PointDetail.damageRefund(
                userId,
                request.orderId(),
                request.refundPoint(),
                newExpiredDate
        ));

        User user = userService.getUserById(userId);
        user.addPoint(refundAmount + request.cashPoint());
    }


    /**
     * 포인트 소멸
     */
    @Override
    public void expirePoints(PointDetail pointDetail) {
        int expiredAmount = pointDetail.getRemainingPrice();

        if (expiredAmount <= 0) return;

        pointDetailRepository.save(PointDetail.expired(pointDetail.getUserId(), pointDetail.getRemainingPrice())); // 소멸 내역 기록
        pointDetail.setRemainingPrice(0); // 기존 적립 내역의 잔여 포인트 0으로 수정

        User user = userService.getUserById(pointDetail.getUserId());
        user.subtractPoint(expiredAmount);
    }

    /**
     * 만료되지 않은 기존 적립 포인트의 remainingPrice를 복구
     */
    private void restoreRemainingPoint(Long userId, int restoreAmount) {
        if(restoreAmount == 0) return; // 환불 금액 0원

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
