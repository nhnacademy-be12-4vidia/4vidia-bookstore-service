package com.nhnacademy._vidiabookstoreservice.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminCheckController {

    @GetMapping("/admin/check")
    public ResponseEntity<Void> checkAdmin(){
        return ResponseEntity.ok().build();
    }

}
