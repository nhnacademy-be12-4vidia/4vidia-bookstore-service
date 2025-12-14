package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.user.dto.grade.response.GradeResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.GradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        Long userId = 1L;

        // given
        GradeResponse response = GradeResponse.builder()
                .gradeName("ROYAL")
                .pointRate(3)
                .build();

        // when
        when(gradeService.getGrade(userId)).thenReturn(response);

        // 요청 수행 및 결과 검증
        mockMvc.perform(get("/users/me/grade")
                        .header("X-User-Id", userId) // 헤더 포함
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("user-grade-get", // target에 만들어질 패키지 이름!!! todo: 컨벤션 정해야함
                        requestHeaders( // 요청 헤더 문서화
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        responseFields( // 응답 필드 문서화
                                fieldWithPath("gradeName").description("등급 이름 (예: WELCOME, ROYAL, GOLD, PLATINUM)"),
                                fieldWithPath("pointRate").description("포인트 적립률 (%)")
                        )
                ));
                // todo : document(...) 안에 들어갈 이름을 정하는 규칙
                //  규칙: "{도메인}-{기능}-{HTTP메서드}" 순서로 지으면, 생성된 스니펫들이 폴더 안에서 `보기 좋게 정렬되어 찾기 쉽다`고 합니다.
    }

    @Test
    void updateGrade() throws Exception {
        // given
        // updateGrade는 void 반환

        // when & then
        mockMvc.perform(put("/users/me/grade/{gradeId}", 2L) // 경로 변수 포함
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent())
                .andDo(document("user-grade-put",
                        requestHeaders( // 요청 헤더 문서화
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        pathParameters( // 경로 변수(Path Variable) 문서화
                                parameterWithName("gradeId").description("변경할 등급 ID")
                        )
                ));
    }
}
