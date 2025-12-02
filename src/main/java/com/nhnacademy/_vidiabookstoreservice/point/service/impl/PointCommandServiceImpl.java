package com.nhnacademy._vidiabookstoreservice.point.service.impl;


import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointPolicyRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointRefundRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointUseRequest;
import com.nhnacademy._vidiabookstoreservice.point.repository.PointDetailRepository;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PointCommandServiceImpl implements PointCommandService {

    private final PointDetailRepository pointDetailRepository;

    /**
     *  1. 주문완료로 기본 적립
     */
    @Override
    public void reward(PointRewardRequest request,Long userId
                       ) {
        // 여기서 유효성 검증, 정책 조회, reason 결정, 저장까지 처리
        PointDetail detail = PointDetail.reward(
                userId,
                request.orderId(),
                request.amount()
        );
        pointDetailRepository.save(detail);
    }


    /**
     * 2. 주문 시 포인트 사용 (차감)
     */
    @Override
    public void use(PointUseRequest request,Long userId) {
        PointDetail detail = PointDetail.use(
                userId,
                request.orderId(),
                request.amount()
        );
        pointDetailRepository.save(detail);
    }

    /**
     * 3. 주문 취소 환불 시 포인트 적립
     */
    public void refund(PointRefundRequest request, Long userId) {
        PointDetail detail = PointDetail.refund(
                userId,
                request.orderId(),
                request.amount()
        );
        pointDetailRepository.save(detail);
    }

    /**
     * 4. 포인트 정책에 따른 적립
     */

    public void rewardByPolicy(PointPolicyRewardRequest request, Long userId) {
        PointDetail detail = PointDetail.rewardByPolicy(
                userId,
                request.orderId(),
                request.policyId(),
                request.amount()
        );
        pointDetailRepository.save(detail);
    }

}
