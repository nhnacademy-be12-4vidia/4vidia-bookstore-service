package com.nhnacademy._vidiabookstoreservice.point.controller;


import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointHistoryResponse;
import com.nhnacademy._vidiabookstoreservice.point.service.PointQueryService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/points")
public class PointQueryController {
    private final PointQueryService queryService;
    private final PointQueryService pointQueryService;

    // 보유 포인트 조회
    @GetMapping("/remain")
    public ResponseEntity<Integer> getRemainPoint(
            @RequestHeader("X-USER-ID") Long userId
    ){
        int remainPoint = queryService.getRemainPoint(userId);
        return ResponseEntity.ok(remainPoint);
    }

    // 소멸 예정 포인트 조회 (30일 이내)
    @GetMapping("/expire-soon")
    public ResponseEntity<Integer> getExpireSoon(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam(defaultValue = "30") int days
    ){
        int result = pointQueryService.getExpiringPointWithinDays(userId, days);
        return ResponseEntity.ok(result);
    }

    // 포인트 전체 내역 조회

    @GetMapping("/history")
    public ResponseEntity<Page<PointHistoryResponse>> getHistory(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(queryService.getHistory(userId, page, size));
    }
}
