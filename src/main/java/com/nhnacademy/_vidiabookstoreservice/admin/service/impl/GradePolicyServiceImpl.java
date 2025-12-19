package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.dto.request.GradePolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.GradePolicyResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.GradePolicyService;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.GradeNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GradePolicyServiceImpl implements GradePolicyService {
    private final GradeRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<GradePolicyResponse> getAll() {
        return repository.findAll().stream()
                .map(GradePolicyResponse::from)
                .toList();
    }

    @Override
    public void update(Long gradeId, GradePolicyUpdateRequest gradePolicyUpdateRequest) {
        Grade grade = repository.findById(gradeId)
                .orElseThrow(() -> new GradeNotFoundException(gradeId));
        grade.updateGrade(gradePolicyUpdateRequest.pointRate());
    }
}
