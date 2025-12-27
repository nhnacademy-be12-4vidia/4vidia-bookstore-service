package com.nhnacademy._vidiabookstoreservice.point.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointHistoryResponse;
import com.nhnacademy._vidiabookstoreservice.point.service.PointQueryService;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PointQueryControllerTest extends SupportControllerTest {

    @MockitoBean
    private PointQueryService pointQueryService;

    @MockitoBean
    private UserService userService;

    private final Long testUserId = 1L;

    @Test
    @DisplayName("[보유 포인트 조회]")
    void getRemainPoint() throws Exception {
        int expectedPoint = 5000;
        given(userService.getUserByPoint(testUserId)).willReturn(expectedPoint);

        mockMvc.perform(get("/users/me/points/remain")
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(expectedPoint))
                .andDo(document("point-remain-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("회원 ID")
                        ),
                        responseFields(
                                fieldWithPath("totalPrice").description("현재 보유 포인트 총합")
                        )
                ));
    }

    @Test
    @DisplayName("[소멸 예정 포인트 조회]")
    void getExpireSoon() throws Exception {
        int expirePoint = 100;
        int days = 7;
        given(pointQueryService.getExpiringPointWithinDays(testUserId, days)).willReturn(expirePoint);

        mockMvc.perform(get("/users/me/points/expire-soon")
                        .header("X-User-Id", testUserId)
                        .param("days", String.valueOf(days))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expirePoint").value(expirePoint))
                .andDo(document("point-expire-soon-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("회원 ID")
                        ),
                        queryParameters(
                                parameterWithName("days").description("조회할 기간 (일 수, 기본값 7)").optional()
                        ),
                        responseFields(
                                fieldWithPath("expirePoint").description("기간 내 소멸 예정 포인트 합계")
                        )
                ));
    }

    @Test
    @DisplayName("[포인트 내역 조회 - 기본 파라미터]")
    void getHistory_default() throws Exception {
        PointHistoryResponse history = new PointHistoryResponse(
                LocalDateTime.now(),
                1000,
                "EARN",
                "구매 적립",
                LocalDate.now().plusYears(1)
        );
        Page<PointHistoryResponse> page = new PageImpl<>(List.of(history), PageRequest.of(0, 10), 1);

        given(pointQueryService.getHistory(eq(testUserId), anyString(), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt()))
                .willReturn(page);

        mockMvc.perform(get("/users/me/points/history")
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("point-history-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("회원 ID")
                        ),
                        queryParameters(
                                parameterWithName("category").description("카테고리 필터 (ALL, EARN, USE 등)").optional(),
                                parameterWithName("from").description("조회 시작일 (YYYY-MM-DD)").optional(),
                                parameterWithName("to").description("조회 종료일 (YYYY-MM-DD)").optional(),
                                parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        ),
                        relaxedResponseFields(
                                fieldWithPath("content[].createdAt").description("생성 일시"),
                                fieldWithPath("content[].price").description("포인트 금액"),
                                fieldWithPath("content[].reason").description("변동 사유"),
                                fieldWithPath("content[].policyName").description("내용/정책명"),
                                fieldWithPath("content[].expiredDate").description("만료 예정일").optional(),

                                fieldWithPath("totalElements").description("전체 데이터 수"),
                                fieldWithPath("totalPages").description("전체 페이지 수"),
                                fieldWithPath("size").description("페이지 크기"),
                                fieldWithPath("number").description("현재 페이지 번호 (0부터 시작)"),
                                fieldWithPath("first").description("첫 페이지 여부"),
                                fieldWithPath("last").description("마지막 페이지 여부"),
                                fieldWithPath("numberOfElements").description("현재 조회된 데이터 수"),
                                fieldWithPath("empty").description("데이터 비어있음 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[포인트 내역 조회 - 날짜 역전 방어 로직 검증]")
    void getHistory_dateSwap() throws Exception {
        String fromStr = "2024-02-01"; // 나중 날짜
        String toStr = "2024-01-01";   // 먼저 날짜 (사용자가 실수로 입력)

        LocalDate expectedStart = LocalDate.parse(toStr);
        LocalDate expectedEnd = LocalDate.parse(fromStr);

        Page<PointHistoryResponse> page = new PageImpl<>(List.of());

        given(pointQueryService.getHistory(eq(testUserId), eq("ALL"), eq(expectedStart), eq(expectedEnd), anyInt(), anyInt()))
                .willReturn(page);

        mockMvc.perform(get("/users/me/points/history")
                        .header("X-User-Id", testUserId)
                        .param("from", fromStr)
                        .param("to", toStr)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}