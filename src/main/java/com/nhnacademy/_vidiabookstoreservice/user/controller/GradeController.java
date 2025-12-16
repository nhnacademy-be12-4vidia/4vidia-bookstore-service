package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
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
    public ResponseEntity<GradeResponse> getGrade() {
        Long userId = UserContext.get().getUserId();

        return ResponseEntity.ok().body(gradeService.getGrade(userId)); // 200 OK + JSON
    }

    /**
     * 등급 변경
     */
    @PutMapping("/{grade-id}")
    public ResponseEntity<String> updateGrade(@PathVariable("grade-id") Long gradeId) {
        Long userId = UserContext.get().getUserId();

        gradeService.updateGrade(userId, gradeId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}