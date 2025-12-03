package com.nhnacademy._vidiabookstoreservice.global.client;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.CouponRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderPageCouponResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "4vidia-coupon-service")
public interface CouponClient {

    @PostMapping("/coupons") //주소 매핑 확인
    List<OrderPageCouponResponse> getUserCoupons(@RequestBody CouponRequest couponRequest);

}
