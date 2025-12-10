package com.nhnacademy._vidiabookstoreservice.global.client;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.CouponRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.CouponUseRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderPageCouponResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "4vidia-coupon-service")
public interface CouponClient {

    @PostMapping("/coupons/validate") //주문전 적용가능한 쿠폰 받아오기
    List<OrderPageCouponResponse> getUserCoupons(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CouponRequest couponRequest);

    @PostMapping("/coupons/use")
    void useCoupon(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CouponUseRequest couponUseRequest);


    // 회원가입 시 welcome 쿠폰 요청
    @PostMapping("/coupons/welcome")
    void getRegisterCoupon(
            @RequestHeader("X-User-Id") Long userId
    );

}
