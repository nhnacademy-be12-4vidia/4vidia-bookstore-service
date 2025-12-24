package com.nhnacademy._vidiabookstoreservice.point.controller;

import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointExpireSoon;
import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointHistoryResponse;
import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointTotalResponse;
import com.nhnacademy._vidiabookstoreservice.point.service.PointQueryService;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/points")
public class PointQueryController {
    private final PointQueryService queryService;
    private final PointQueryService pointQueryService;
    private final UserService userService;

    // 보유 포인트 조회
    @GetMapping("/remain")
    public ResponseEntity<PointTotalResponse> getRemainPoint(
    ){
        Long userId = UserContext.get().getUserId();
        int remainPoint = userService.getUserByPoint(userId);
        return ResponseEntity.ok().body(new PointTotalResponse(remainPoint));
    }

    // 소멸 예정 포인트 조회 (30일 이내)
    @GetMapping("/expire-soon")
    public ResponseEntity<PointExpireSoon> getExpireSoon(
            @RequestParam(defaultValue = "7") int days
    ){
        Long userId = UserContext.get().getUserId();
        int result = pointQueryService.getExpiringPointWithinDays(userId, days);
        return ResponseEntity.ok().body(new PointExpireSoon(result));
    }

    // 포인트 전체 내역 조회

//    @GetMapping("/history")
//    public ResponseEntity<Page<PointHistoryResponse>> getHistory(
//            @RequestParam(defaultValue = "ALL") String category,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size
//    ){
//        Long userId = UserContext.get().getUserId();
//        return ResponseEntity.ok(queryService.getHistory(userId, category,page, size));
//    }
@GetMapping("/history")
public ResponseEntity<Page<PointHistoryResponse>> getHistory(
        @RequestParam(defaultValue = "ALL") String category,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) {
    Long userId = UserContext.get().getUserId();

    // ✅ 기간 기본값 (프론트에서 안 보내도 동작)
    LocalDate end = (to != null) ? to : LocalDate.now();
    LocalDate start = (from != null) ? from : end.minusMonths(3);

    // ✅ from > to 방어
    if (start.isAfter(end)) {
        LocalDate tmp = start;
        start = end;
        end = tmp;
    }

    return ResponseEntity.ok(queryService.getHistory(userId, category, start, end, page, size));
}




}
