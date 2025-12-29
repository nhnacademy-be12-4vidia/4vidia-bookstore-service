package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.user.dto.grade.response.GradeResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.GradeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GradeControllerTest extends SupportControllerTest {

    @MockitoBean
    private GradeService gradeService;

    @Test
    @DisplayName("[내 등급 조회]")
    void getGrade() throws Exception {
        Long userId = 1L;
        GradeResponse response = new GradeResponse("PLATINUM", 5);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(gradeService.getGrade(userId)).willReturn(response);

            mockMvc.perform(get("/users/me/grade")
                            .header("X-User-Id", userId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.gradeName").value("PLATINUM"))
                    .andDo(document("grade-get",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(headerWithName("X-User-Id").description("회원 식별 ID")),
                            responseFields(withHeader(
                                    fieldWithPath("data.gradeName").description("등급 이름"),
                                    fieldWithPath("data.pointRate").description("포인트 적립률")
                            ))
                    ));
        }
    }

    @Test
    @DisplayName("[등급 변경]")
    void updateGrade() throws Exception {
        // Given
        Long userId = 1L;
        Long gradeId = 3L;

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            doNothing().when(gradeService).updateGrade(eq(userId), eq(gradeId));

            // When & Then
            mockMvc.perform(put("/users/me/grade/{grade-id}", gradeId)
                            .header("X-User-Id", userId))
                    .andExpect(status().isOk())
                    .andDo(document("grade-update-put",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(headerWithName("X-User-Id").description("회원 식별 ID")),
                            pathParameters(parameterWithName("grade-id").description("변경할 등급 ID")),
                            responseFields(withHeader()) // 응답 바디가 비어있어도 ApiResponse 공통 필드 검증
                    ));

            // 실제로 서비스가 호출되었는지 검증 (선택사항)
            verify(gradeService, times(1)).updateGrade(eq(userId), eq(gradeId));
        }
    }
}