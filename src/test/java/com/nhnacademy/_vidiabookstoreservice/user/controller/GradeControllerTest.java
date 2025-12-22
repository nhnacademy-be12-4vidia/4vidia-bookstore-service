package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GradeControllerTest extends SupportControllerTest {

    @Autowired private GradeRepository gradeRepository;
    @Autowired private UserRepository userRepository;

    private Long testUserId;
    private Long targetGradeId;

    @BeforeEach
    void initData() {

        Grade welcomeGrade = Grade.builder()
                .gradeName(GradeName.WELCOME)
                .pointRate(1)
                .build();
        gradeRepository.save(welcomeGrade);

        Grade goldGrade = Grade.builder()
                .gradeName(GradeName.GOLD)
                .pointRate(3)
                .build();
        gradeRepository.save(goldGrade);
        this.targetGradeId = goldGrade.getGradeId();

        User user = User.builder()
                .email("grade_test@h2.com")
                .password("password")
                .name("등급테스트유저")
                .phone("01011112222")
                .birthDate(LocalDate.now())
                .grade(welcomeGrade)
                .build();
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        this.testUserId = savedUser.getUserId();
    }

    @Test
    @DisplayName("[등급 조회]")
    void getGrade() throws Exception {
        mockMvc.perform(get("/users/me/grade")
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeName").value("WELCOME"))
                .andDo(document("user-grade-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        responseFields(
                                fieldWithPath("gradeName").description("등급 이름 (예: WELCOME, ROYAL, GOLD, PLATINUM)"),
                                fieldWithPath("pointRate").description("포인트 적립률 (%)")
                        )
                ));
    }

    @Test
    @DisplayName("[등급 변경]")
    void updateGrade() throws Exception {
        mockMvc.perform(put("/users/me/grade/{grade-id}", targetGradeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNoContent())
                .andDo(document("user-grade-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        pathParameters(
                                parameterWithName("grade-id").description("변경할 등급 ID")
                        )
                ));
    }
}