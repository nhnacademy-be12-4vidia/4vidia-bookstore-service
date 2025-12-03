package com.nhnacademy._vidiabookstoreservice.point.service;

import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointPolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointPolicyResponse;

import java.util.List;

public interface PointPolicyService {
    PointPolicyResponse get(Long pointPolicyId);
    List<PointPolicyResponse> getAll();
    PointPolicyResponse update(Long pointPolicyId, PointPolicyUpdateRequest request);
}
