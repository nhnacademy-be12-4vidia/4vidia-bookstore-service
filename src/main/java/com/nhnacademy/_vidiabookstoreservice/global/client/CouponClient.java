package com.nhnacademy._vidiabookstoreservice.global.client;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.CouponCalculationRequest;
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
    // 근데 실제로 헤더는 누락 가능성이 있어서 DTO에 명시적으로 담아 처리하는 것이 더 안전함
    // 하지만 이번 프로젝트에서 유저아이디는 헤더로만 받는걸로

    @PostMapping("/coupons/validate") //주문전 적용가능한/불가능한 쿠폰 모두 받아오기
    List<OrderPageCouponResponse> getUserCoupons(@RequestHeader("X-User-Id") Long userId,
                                                 @RequestBody CouponRequest couponRequest);

    // TODO [주문] 주문 저장전 값 검증할때는 뭘 보내고 받으면 좋을까?
    @PostMapping("/coupons/validate-order") //실제 선택된 쿠폰 검증하기
    int calculateCoupons(@RequestHeader("X-User-Id") Long userId,
                         @RequestBody CouponCalculationRequest couponCalculationRequest);

    @PostMapping("/coupons/use")
    void useCoupon(@RequestHeader("X-User-Id") Long userId,
                   @RequestBody CouponUseRequest couponUseRequest);


    // 회원가입 시 welcome 쿠폰 요청
    @PostMapping("/coupons/welcome")
    void getRegisterCoupon(@RequestHeader("X-User-Id") Long userId);

}
