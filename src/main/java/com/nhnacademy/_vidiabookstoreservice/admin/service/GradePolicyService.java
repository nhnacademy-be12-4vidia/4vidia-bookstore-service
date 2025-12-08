package com.nhnacademy._vidiabookstoreservice.admin.service;

import com.nhnacademy._vidiabookstoreservice.admin.dto.request.GradePolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.GradePolicyResponse;

import java.util.List;

public interface GradePolicyService {

    List<GradePolicyResponse> getAll();
    void update(Long gradeId, GradePolicyUpdateRequest gradePolicyUpdateRequest);

}
