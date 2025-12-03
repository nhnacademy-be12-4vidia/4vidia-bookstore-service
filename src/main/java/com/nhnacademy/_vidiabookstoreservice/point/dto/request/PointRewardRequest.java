package com.nhnacademy._vidiabookstoreservice.point.dto.request;

public record PointRewardRequest (
    Long orderId,
    int amount
){

}
