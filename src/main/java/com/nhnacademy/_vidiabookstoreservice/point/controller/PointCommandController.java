package com.nhnacademy._vidiabookstoreservice.point.controller;

import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointPolicyRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointRefundRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointOrderRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointUseRequest;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PointCommandController {

    private final PointCommandService pointCommandService;

    /** 1. 주문 완료 기본 적립 */
    @PostMapping("/users/me/points/reward")
    public ResponseEntity<Void> reward(
            @Valid @RequestBody PointOrderRewardRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        pointCommandService.reward(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /** 2. 주문 시 포인트 사용 (차감) */
    @PostMapping("/users/me/points/use")
    public ResponseEntity<Void> use(
            @Valid @RequestBody PointUseRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        pointCommandService.use(request, userId);
        return ResponseEntity.ok().build();
    }

    /** 3. 결제 비정상 처리 || 결제 취소 */
    @PostMapping("/users/me/points/cancel")
    public ResponseEntity<Void> cancelUse(@RequestBody Long orderId,
                                          @RequestHeader("X-User-Id") Long userId
    ){
        pointCommandService.cancelUse(orderId, userId);
        return ResponseEntity.ok().build();
    }


    /** 3. 반품 — 차감되었던 포인트 복구 (적립) */
    @PostMapping("/users/me/points/refund")
    public ResponseEntity<Void> refund(
            @Valid @RequestBody PointRefundRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        pointCommandService.refund(request, userId);
        return ResponseEntity.ok().build();
    }

    /** 4. 정책 기준 적립 — 회원가입/리뷰/포토 리뷰 */
    @PostMapping("/points/signup")
    public ResponseEntity<Void> rewardBySignUp(
            @Valid @RequestBody PointPolicyRewardRequest request
    ) {
        pointCommandService.rewardByPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/users/me/points/review")
    public ResponseEntity<Void> rewardByReview(
            @Valid @RequestBody PointPolicyRewardRequest request
    ) {
        pointCommandService.rewardByPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
