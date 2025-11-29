package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.user.dto.response.GradeResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/my/grades")
public class GradeController {
    private final GradeService gradeService;

    /**
     * 등급 조회
     */
    @GetMapping
    public ResponseEntity<GradeResponse> getGrade(@RequestHeader("X-USER-ID") Long id) {
        return ResponseEntity.ok(gradeService.getGrade(id));
    }


    /**
     * 등급 변경
     */
    @PutMapping("/{gradeId}")
    public ResponseEntity<String> updateGrade(@RequestHeader("X-USER-ID") Long id,
                                              @PathVariable Long gradeId) {
        gradeService.updateGrade(id, gradeId);
        return ResponseEntity.ok("등급 변경 완료");
    }

}