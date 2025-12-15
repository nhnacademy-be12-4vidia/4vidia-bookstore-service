package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.user.dto.grade.response.GradeResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users/me/grade") // 기존 "/my/grades"
public class GradeController {
    private final GradeService gradeService;

    /**
     * 등급 조회
     */
    @GetMapping
    public ResponseEntity<GradeResponse> getGrade(@RequestHeader("X-User-Id") Long id) {
        return ResponseEntity.ok().body(gradeService.getGrade(id)); // 200 OK + JSON
    }

    /**
     * 등급 변경
     */
    @PutMapping("/{grade-id}")
    public ResponseEntity<String> updateGrade(@RequestHeader("X-User-Id") Long id,
                                              @PathVariable("grade-id") Long gradeId) {

        // todo : 아무때나 변경되면 안됨
        //  3개월 이내 순수 주문금액을 기준
        //  순수 금액 = 주문 금액 - (쿠폰+배송비+취소금액+포장비)
        gradeService.updateGrade(id, gradeId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}