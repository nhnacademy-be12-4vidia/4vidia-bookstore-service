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
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    public void reward(PointOrderRewardRequest request, Long userId
                       ) {
        // 여기서 유효성 검증, 정책 조회, reason 결정, 저장까지 처리
        PointDetail detail = PointDetail.reward(
                userId,
                request.orderId(),
                request.amount()
        );
        pointDetailRepository.save(detail);
        userRepository.findById(userId)
                .ifPresent(user-> user.addPoint(request.amount()));
    }


    /**
     * 2. 주문 시 포인트 사용 (차감)
     */
    @Transactional
    @Override
    public void use(PointUseRequest request,Long userId) {
        int remaining = request.amount(); // 사용자가 작성한 포인트 사용 금액
        if(remaining <= 0) {
            throw new IllegalArgumentException("사용금액은 0보다 커야 합니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        List<PointDetail> avaiableList =
                pointDetailRepository.findAvailablePointForUse(userId, now);

        for(PointDetail detail : avaiableList) {
            if(remaining <= 0) {
                break;
            }
            int available = detail.getPrice();

            if(available >= remaining) {
                // 현 적립 포인트에서 모두 차감 가능
                detail.decrease(remaining);
                pointDetailRepository.save(PointDetail.use(
                        userId,
                        request.orderId(),
                        remaining
                ));
                remaining = 0;
            }else{
                // 현 적립 포인트 모두 소모 -> 다음 적립 레코드로 이동
                detail.decrease(remaining);
                pointDetailRepository.save(PointDetail.use(
                        userId,
                        request.orderId(),
                        available
                ));
                remaining -= available;
            }
        }
        if(remaining>0){
            throw new IllegalArgumentException("보유포인트가 부족합니다.");
        }
        userRepository.findById(userId)
                .ifPresent(user-> user.subtractPoint(request.amount()));
    }

    // TODO 결제 취소는 포인트로 들어오지 않는데 이건 refund를 말하는건지 결제 취소를 말하는건지..?
    /**
     * 3. 주문 취소 환불 시 포인트 적립
     */
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

        // 환불  = 사용 취소이므로 환불 내역만 추가하면 된다.
        PointDetail detail = PointDetail.refund(
                userId,
                request.orderId(),
                refundAmount
        );
        pointDetailRepository.save(detail);
        userRepository.findById(userId)
                .ifPresent(user-> user.addPoint(request.amount()));
    }

    /**
     * 4. 포인트 정책에 따른 적립
     */

    public void rewardByPolicy(PointPolicyRewardRequest request) {
        PointDetail detail = PointDetail.rewardByPolicy(
                request.userId(),
                pointPolicyRepository.findByPointPolicyId((request.policyId()))
        );
        pointDetailRepository.save(detail);
    }





}
