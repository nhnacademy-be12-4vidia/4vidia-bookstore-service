package com.nhnacademy._vidiabookstoreservice.point.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointExpireSoon;
import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointHistoryResponse;
import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointTotalResponse;
import com.nhnacademy._vidiabookstoreservice.point.service.PointQueryService;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PointQueryControllerTest extends SupportControllerTest {

    @MockitoBean
    private PointQueryService queryService;

    @MockitoBean
    private PointQueryService pointQueryService;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("[보유 포인트 조회]")
    void getRemainPoint() throws Exception {
        Long userId = 1L;
        int remainPoint = 5000;

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(userService.getUserByPoint(userId)).willReturn(remainPoint);

            mockMvc.perform(get("/users/me/points/remain")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalPrice").value(remainPoint))
                    .andDo(document("point-remain-get",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(withHeader(
                                    fieldWithPath("data.totalPrice").description("현재 보유 포인트 총액")
                            ))
                    ));
        }
    }

    @Test
    @DisplayName("[소멸 예정 포인트 조회]")
    void getExpireSoon() throws Exception {
        Long userId = 1L;
        int expirePoint = 1000;

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(pointQueryService.getExpiringPointWithinDays(eq(userId), anyInt())).willReturn(expirePoint);

            mockMvc.perform(get("/users/me/points/expire-soon")
                            .param("days", "7")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.expirePoint").value(expirePoint))
                    .andDo(document("point-expire-soon-get",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            queryParameters(
                                    parameterWithName("days").description("조회할 기간(일 단위, 기본값 7)").optional()
                            ),
                            responseFields(withHeader(
                                    fieldWithPath("data.expirePoint").description("기간 내 소멸 예정 포인트 합계")
                            ))
                    ));
        }
    }

    @Test
    @DisplayName("[포인트 전체 내역 조회]")
    void getHistory() throws Exception {
        Long userId = 1L;
        PointHistoryResponse history = new PointHistoryResponse(
                LocalDateTime.now(), 500, "구매 적립", "구매 정책", LocalDate.now().plusYears(1)
        );
        // PageRequest와 함께 PageImpl 생성
        Page<PointHistoryResponse> pageResponse = new PageImpl<>(List.of(history), PageRequest.of(0, 10), 1);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(queryService.getHistory(eq(userId), anyString(), any(), any(), anyInt(), anyInt()))
                    .willReturn(pageResponse);

            mockMvc.perform(get("/users/me/points/history")
                            .param("category", "ALL")
                            .param("from", "2024-01-01")
                            .param("to", LocalDate.now().toString())
                            .param("page", "0")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].price").value(500))
                    .andDo(document("point-history-get",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            queryParameters(
                                    parameterWithName("category").description("내역 카테고리 (ALL, EARNED, USED 등)").optional(),
                                    parameterWithName("from").description("조회 시작일").optional(),
                                    parameterWithName("to").description("조회 종료일").optional(),
                                    parameterWithName("page").description("페이지 번호").optional(),
                                    parameterWithName("size").description("페이지 크기").optional()
                            ),
                            responseFields(withHeader(
                                    fieldWithPath("data.content[].createdAt").description("내역 발생 일시"),
                                    fieldWithPath("data.content[].price").description("포인트 변동액"),
                                    fieldWithPath("data.content[].reason").description("변동 사유"),
                                    fieldWithPath("data.content[].policyName").description("적용 정책명"),
                                    fieldWithPath("data.content[].expiredDate").description("포인트 만료일"),

                                    fieldWithPath("data.pageable.pageNumber").description("현재 페이지 번호"),
                                    fieldWithPath("data.pageable.pageSize").description("페이지 크기"),
                                    fieldWithPath("data.pageable.sort").description("정렬 정보 (배열)"),
                                    fieldWithPath("data.pageable.offset").description("해당 페이지의 시작 오프셋"),
                                    fieldWithPath("data.pageable.paged").description("페이징 정보 포함 여부"),
                                    fieldWithPath("data.pageable.unpaged").description("페이징 정보 미포함 여부"),

                                    fieldWithPath("data.last").description("마지막 페이지 여부"),
                                    fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                    fieldWithPath("data.totalElements").description("전체 데이터 수"),
                                    fieldWithPath("data.size").description("페이지당 데이터 수"),
                                    fieldWithPath("data.number").description("현재 페이지 번호"),
                                    fieldWithPath("data.sort").description("정렬 정보 (배열)"),
                                    fieldWithPath("data.first").description("첫 페이지 여부"),
                                    fieldWithPath("data.numberOfElements").description("현재 페이지의 엘리먼트 수"),
                                    fieldWithPath("data.empty").description("결과가 비어있는지 여부")
                            ))
                    ));
        }
    }
}