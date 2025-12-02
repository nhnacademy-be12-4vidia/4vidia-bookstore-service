package com.nhnacademy._vidiabookstoreservice.point.service;

import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointPolicyRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointRefundRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointUseRequest;

public interface PointCommandService {
    void reward(PointRewardRequest request, Long userId);
    void use(PointUseRequest request, Long userId);
    void refund(PointRefundRequest request, Long userId);
    void rewardByPolicy(PointPolicyRewardRequest request, Long userId);


}
