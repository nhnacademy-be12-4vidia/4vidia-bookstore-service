package com.nhnacademy._vidiabookstoreservice.global.client;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.CouponCalculationRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.CouponUseRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.CouponCalculationResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundCouponRequest;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.UseCouponResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.coupon.response.ActivePolicyIdResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "4vidia-coupon-service")
public interface CouponClient {
    // 근데 실제로 헤더는 누락 가능성이 있어서 DTO에 명시적으로 담아 처리하는 것이 더 안전함
    // 하지만 이번 프로젝트에서 유저아이디는 헤더로만 받는걸로

    @PostMapping("/coupons/calculate") //실제 선택된 쿠폰 검증하기
    CouponCalculationResponse calculateCoupons(@RequestHeader("X-User-Id") Long userId,
                                               @RequestBody CouponCalculationRequest couponCalculationRequest);

    @PostMapping("/coupons/use")
    void useCoupon(@RequestHeader("X-User-Id") Long userId,
                   @RequestBody CouponUseRequest couponUseRequest);


    // 회원가입 시 welcome 쿠폰 요청
    @PostMapping("/coupons/welcome")
    void getRegisterCoupon(@RequestHeader("X-User-Id") Long userId);

    //회원가입 시 birthday 쿠폰 요청
    @PostMapping("/policies/birthday")
    void getRegisterBirthdayCoupon(@RequestHeader("X-User-Id") Long userId);

    // 반품 : 주문에 사용한 쿠폰 정보 요청
    @PostMapping("/coupons/refund")
    UseCouponResponse getUseCouponDetail(@RequestBody RefundCouponRequest refundCouponRequest);

    @GetMapping("/policies/active")
    ResponseEntity<ActivePolicyIdResponse> getActivePolicy(
            @RequestParam String policyType
    );

}
