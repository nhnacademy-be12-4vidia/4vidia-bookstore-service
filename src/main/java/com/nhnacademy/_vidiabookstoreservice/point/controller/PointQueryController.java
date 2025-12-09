package com.nhnacademy._vidiabookstoreservice.point.controller;

import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointExpireSoon;
import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointHistoryResponse;
import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointTotalResponse;
import com.nhnacademy._vidiabookstoreservice.point.service.PointQueryService;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/my/points")
public class PointQueryController {
    private final PointQueryService queryService;
    private final PointQueryService pointQueryService;
    private final UserService userService;

    // 보유 포인트 조회
    @GetMapping("/remain")
    public ResponseEntity<PointTotalResponse> getRemainPoint(
            @RequestHeader("X-User-Id") Long userId
    ){
        int remainPoint = userService.getUserByPoint(userId);
        return ResponseEntity.ok().body(new PointTotalResponse(remainPoint));
    }

    // 소멸 예정 포인트 조회 (30일 이내)
    @GetMapping("/expire-soon")
    public ResponseEntity<PointExpireSoon> getExpireSoon(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "7") int days
    ){
        int result = pointQueryService.getExpiringPointWithinDays(userId, days);
        return ResponseEntity.ok().body(new PointExpireSoon(result));
    }

    // 포인트 전체 내역 조회

    @GetMapping("/history")
    public ResponseEntity<Page<PointHistoryResponse>> getHistory(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(queryService.getHistory(userId, page, size));
    }
}
