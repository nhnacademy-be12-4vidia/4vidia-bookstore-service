package com.nhnacademy._vidiabookstoreservice.point.service.impl;

import com.nhnacademy._vidiabookstoreservice.point.domain.PointPolicy;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointPolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointPolicyResponse;
import com.nhnacademy._vidiabookstoreservice.point.exception.PointPolicyNotFoundException;
import com.nhnacademy._vidiabookstoreservice.point.repository.PointPolicyRepository;
import com.nhnacademy._vidiabookstoreservice.point.service.PointPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PointPolicyServiceImpl implements PointPolicyService {

    private final PointPolicyRepository repository;

    /**
     * 포인트 정책 조회 (단건)
     * @param pointPolicyId
     * @return PointPolicyResponse(pointPolicyId, pointPolicyName, price)
     */
    @Override
    @Transactional(readOnly = true)
    public PointPolicyResponse get(Long pointPolicyId) {
        PointPolicy policy = repository.findById(pointPolicyId)
                .orElseThrow(() -> new PointPolicyNotFoundException(pointPolicyId)); // => NOT_FOUND(404)
        return PointPolicyResponse.from(policy);
    }

    /**
     * 포인트 리스트 조회
     * @return List<PointPolicyResponse>
     */

    @Override
    @Transactional(readOnly = true)
    public List<PointPolicyResponse> getAll() {
        return repository.findAll().stream()
                .map(PointPolicyResponse::from)
                .toList();
    }

    /**
     * 포인트 정책 수정 (포인트 금액만 수정 가능)
     * @param pointPolicyId
     * @param request (price)
     * @return PointPolicyResponse(pointPolicyId, pointPolicyName, price)
     */
    @Override
    public PointPolicyResponse update(Long pointPolicyId, PointPolicyUpdateRequest request) {
        PointPolicy policy = repository.findById(pointPolicyId)
                .orElseThrow(() -> new PointPolicyNotFoundException(pointPolicyId));

        policy.updatePrice(request.price());

        return PointPolicyResponse.from(policy);
    }

}

