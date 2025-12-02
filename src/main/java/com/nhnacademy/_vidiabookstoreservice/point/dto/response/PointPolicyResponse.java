package com.nhnacademy._vidiabookstoreservice.point.dto.response;

import com.nhnacademy._vidiabookstoreservice.point.domain.PointPolicy;

public record PointPolicyResponse(
        Long pointPolicyId,
        String pointName,
        Integer price
) {
    public static PointPolicyResponse from(PointPolicy pointPolicy){
        return new PointPolicyResponse(
                pointPolicy.getPointPolicyId(),
                pointPolicy.getPointName(),
                pointPolicy.getPrice()
        );
    }
}

