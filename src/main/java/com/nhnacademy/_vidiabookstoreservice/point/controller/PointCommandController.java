package com.nhnacademy._vidiabookstoreservice.point.controller;

import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointPolicyRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointRefundRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointOrderRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointUseRequest;
import com.nhnacademy._vidiabookstoreservice.point.service.impl.PointCommandServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PointCommandController {

    private final PointCommandServiceImpl pointCommandService;

    /** 1. 주문 완료 기본 적립 */
    @PostMapping("/my/points/reward")
    public ResponseEntity<Void> reward(
            @Valid @RequestBody PointOrderRewardRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        pointCommandService.reward(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /** 2. 주문 시 포인트 사용 (차감) */
    @PostMapping("/my/points/use")
    public ResponseEntity<Void> use(
            @Valid @RequestBody PointUseRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        pointCommandService.use(request, userId);
        return ResponseEntity.ok().build();
    }

    /** 3. 결제 비정상 처리 || 결제 취소 */
    @PostMapping("/my/points/cancel")
    public ResponseEntity<Void> cancelUse(Long orderId,
                                          @RequestHeader("X-User-Id") Long userId
    ){
        pointCommandService.cancelUse(orderId, userId);
        return ResponseEntity.ok().build();
    }


    /** 3. 반품 — 차감되었던 포인트 복구 (적립) */
    @PostMapping("/my/points/refund")
    public ResponseEntity<Void> refund(
            @Valid @RequestBody PointRefundRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        pointCommandService.refund(request, userId);
        return ResponseEntity.ok().build();
    }

    /** 4. 정책 기준 적립 — 회원가입/리뷰/포토 리뷰 */
    // TODO 회원가입은 /my가 없어야 하고, 나머지는 있어야 한다면? 분리? 굳이? 해야하나?
    @PostMapping("/points/policy-reward")
    public ResponseEntity<Void> rewardByPolicy(
            @Valid @RequestBody PointPolicyRewardRequest request
    ) {
        pointCommandService.rewardByPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }



}
