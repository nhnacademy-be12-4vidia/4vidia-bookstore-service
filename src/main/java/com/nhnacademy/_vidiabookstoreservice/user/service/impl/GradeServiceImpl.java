package com.nhnacademy._vidiabookstoreservice.user.service.impl;


import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.dto.grade.response.GradeResponse;
import com.nhnacademy._vidiabookstoreservice.user.exception.GradeNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserNotFoundByUserIdException;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.GradeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class GradeServiceImpl implements GradeService {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;

    /**
     * 등급 조회
     */
    @Override
    public GradeResponse getGrade(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundByUserIdException(userId));

        return GradeResponse.builder()
                .gradeName(user.getGrade().getGradeName().name())
                .pointRate(user.getGrade().getPointRate())
                .build();
    }

    /**
     * 등급 변경
     */
    @Override
    public void updateGrade(Long userId, Long gradeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundByUserIdException(userId));
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new GradeNotFoundException(gradeId));

        user.setGrade(grade);
    }

}
