package com.nhnacademy._vidiabookstoreservice.point.service;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointPolicyRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointRefundCommand;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointUseRequest;

public interface PointCommandService {
    void reward(Order order);

    void expireIfNeeded(Long userId);
    void validateUsablePoint(Long userId, int pointUsed);
    void use(PointUseRequest request, Long userId);

    void cancelUse(Long orderId, Long userId);
    void rewardByPolicy(PointPolicyRewardRequest request);
    void refundSimpleChange(PointRefundCommand request, Long userId);
    void refundDamaged(PointRefundCommand request, Long userId);
    void expirePoints(PointDetail pointDetail);
}