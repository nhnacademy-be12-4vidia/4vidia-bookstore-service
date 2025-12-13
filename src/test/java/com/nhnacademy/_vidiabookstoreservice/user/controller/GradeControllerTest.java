package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.user.dto.grade.response.GradeResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.GradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@ActiveProfiles("local") // 테스트 전용 profile 사용
@SpringBootTest
class GradeControllerTest {

    @MockitoBean
    private GradeService gradeService; // 외부 의존성을 Mock 처리

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    void getGrade() throws Exception {
        // Mock 데이터 생성
        GradeResponse response = new GradeResponse("WELCOME", 1);

        // Mock 동작 정의
        when(gradeService.getGrade(1L)).thenReturn(response);

        // 요청 수행 및 결과 검증
        mockMvc.perform(get("/users/me/grade")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeName").value(response.gradeName()))
                .andExpect(jsonPath("$.pointRate").value(response.pointRate()))
                .andDo(document("get-grade"));
    }

    @Test
    void updateGrade() throws Exception {
        // updateGrade는 void 반환이므로 Mock 설정만
        // gradeService.updateGrade(1L, 2L)를 호출해도 실제 동작은 없음

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/users/me/grade/2")
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent())
                .andDo(document("put-grade"));
    }
}
