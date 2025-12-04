package com.nhnacademy._vidiabookstoreservice.point.controller;

import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointPolicyRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointRefundRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointUseRequest;
import com.nhnacademy._vidiabookstoreservice.point.service.impl.PointCommandServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/points")
public class PointCommandController {

    private final PointCommandServiceImpl pointCommandService;

    /** 1. 주문 완료 기본 적립 */
    @PostMapping("/reward")
    public ResponseEntity<Void> reward(
            @Valid @RequestBody PointRewardRequest request,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        pointCommandService.reward(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /** 2. 주문 시 포인트 사용 (차감) */
    @PostMapping("/use")
    public ResponseEntity<Void> use(
            @Valid @RequestBody PointUseRequest request,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        pointCommandService.use(request, userId);
        return ResponseEntity.ok().build();
    }


    /** 3. 주문 취소 환불 — 차감되었던 포인트 복구 (적립) */
    @PostMapping("/refund")
    public ResponseEntity<Void> refund(
            @Valid @RequestBody PointRefundRequest request,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        pointCommandService.refund(request, userId);
        return ResponseEntity.ok().build();
    }

    /** 4. 정책 기준 적립 — 이벤트/프로모션/등급/캠페인 */
    @PostMapping("/policy-reward")
    public ResponseEntity<Void> rewardByPolicy(
            @Valid @RequestBody PointPolicyRewardRequest request
    ) {
        pointCommandService.rewardByPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
