package com.nhnacademy._vidiabookstoreservice.point.dto.request;

public record PointOrderRewardRequest(
    Long orderId,
    int price
){

}
