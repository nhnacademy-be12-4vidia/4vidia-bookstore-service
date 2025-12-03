package com.nhnacademy._vidiabookstoreservice.admin.service;

import com.nhnacademy._vidiabookstoreservice.admin.dto.request.PointPolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.PointPolicyResponse;

import java.util.List;

public interface PointPolicyService {
    PointPolicyResponse get(Long pointPolicyId);
    List<PointPolicyResponse> getAll();
    void update(Long pointPolicyId, PointPolicyUpdateRequest request);
}
