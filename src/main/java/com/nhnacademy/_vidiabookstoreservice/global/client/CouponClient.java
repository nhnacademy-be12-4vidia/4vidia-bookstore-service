package com.nhnacademy._vidiabookstoreservice.global.client;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.CouponRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.CouponUseRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderPageCouponResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "4vidia-coupon-service")
public interface CouponClient {

    //TODO 주소매핑 확인
    @PostMapping("/coupons") //주문전 적용가능한 쿠폰 받아오기
    List<OrderPageCouponResponse> getUserCoupons(@RequestBody CouponRequest couponRequest);

    @PostMapping("/coupons/use")
    void useCoupon(@RequestBody CouponUseRequest couponUseRequest);

//    @PostMapping("/coupons/cancel")
//    void cancelCoupon()
}
