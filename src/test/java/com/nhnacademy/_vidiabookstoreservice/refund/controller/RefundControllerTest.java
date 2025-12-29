package com.nhnacademy._vidiabookstoreservice.refund.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundItemStatus;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundRequest;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.OrderItemResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundCountResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundHistoryGroupResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundResponse;
import com.nhnacademy._vidiabookstoreservice.refund.service.RefundService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RefundControllerTest extends SupportControllerTest {

    @MockitoBean
    private RefundService refundService;

    @Test
    @DisplayName("[반품 가능 도서 목록 조회]")
    void getRefundList() throws Exception {
        // Given
        long orderId = 100L;
        OrderItemResponse item = new OrderItemResponse(50L, 1L, "테스트 책", 2);

        RefundResponse response = new RefundResponse(orderId, true, List.of(item));

        given(refundService.getRefundList(orderId)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/orders/{order-id}/refunds", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andDo(document("refund-available-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("order-id").description("주문 ID")),
                        responseFields(withHeader(
                                fieldWithPath("data.orderId").description("주문 ID"),
                                fieldWithPath("data.canReturnByChangeOfMind").description("단순 변심 반품 가능 여부 (7일 이내 여부 등)"),
                                fieldWithPath("data.orderItems[]").description("반품 가능 도서 목록"),
                                fieldWithPath("data.orderItems[].orderItemId").description("주문 상세 아이템 ID"),
                                fieldWithPath("data.orderItems[].bookId").description("도서 ID"),
                                fieldWithPath("data.orderItems[].bookTitle").description("도서 제목"),
                                fieldWithPath("data.orderItems[].quantity").description("수량")
                        ))
                ));
    }

    @Test
    @DisplayName("[반품 신청서 등록]")
    void refundRegister() throws Exception {
        // Given
        RefundRequest request = new RefundRequest(100L, "단순 변심", false, List.of(50L));

        // When & Then
        mockMvc.perform(post("/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("refund-register",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("orderId").description("주문 ID"),
                                fieldWithPath("reason").description("반품 사유"),
                                fieldWithPath("damaged").description("파손 여부"),
                                fieldWithPath("orderItemIds[]").description("반품 신청 아이템 PK 목록")
                        ),
                        responseFields(withHeader())
                ));
    }

    @Test
    @DisplayName("[내 반품 신청 내역 조회]")
    void getMyRefundHistories() throws Exception {
        // Given
        Long userId = 1L;
        RefundHistoryGroupResponse.RefundItemResponse item = new RefundHistoryGroupResponse.RefundItemResponse(
                "테스트 도서", 1, 15000, RefundItemStatus.PROCESS, null
        );

        RefundHistoryGroupResponse history = new RefundHistoryGroupResponse(
                10L, 100L, LocalDate.now().minusDays(3), LocalDate.now(),
                RefundStatus.PROCESS, "사유", 15000, List.of(item)
        );

        Page<RefundHistoryGroupResponse> page = new PageImpl<>(List.of(history), PageRequest.of(0, 10), 1);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(refundService.getMyRefunds(eq(userId), any(), any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/users/me/refunds")
                            .header("X-User-Id", userId)
                            .param("page", "0")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].refundId").value(10L))
                    .andDo(document("refund-my-list",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(headerWithName("X-User-Id").description("회원 ID")),
                            queryParameters(
                                    parameterWithName("status").description("반품 상태 필터 (PROCESS, APPROVED)").optional(),
                                    parameterWithName("page").description("페이지 번호").optional(),
                                    parameterWithName("size").description("페이지 크기").optional()
                            ),
                            responseFields(withHeader(
                                    fieldWithPath("data.content[].refundId").description("반품 번호"),
                                    fieldWithPath("data.content[].orderId").description("주문 번호"),
                                    fieldWithPath("data.content[].orderDate").description("주문 일자"),
                                    fieldWithPath("data.content[].refundDate").description("반품 신청 일자"),
                                    fieldWithPath("data.content[].refundStatus").description("전체 진행 상태"),
                                    fieldWithPath("data.content[].refundReason").description("신청 사유"),
                                    fieldWithPath("data.content[].totalRefundPrice").description("총 환불 예정 금액"),
                                    fieldWithPath("data.content[].items[].title").description("도서 제목"),
                                    fieldWithPath("data.content[].items[].quantity").description("수량"),
                                    fieldWithPath("data.content[].items[].price").description("환불 금액"),
                                    fieldWithPath("data.content[].items[].refundItemStatus").description("아이템별 상태"),
                                    fieldWithPath("data.content[].items[].rejectDetail").description("거절 시 사유").optional(),

                                    fieldWithPath("data.page").description("현재 페이지 번호"),
                                    fieldWithPath("data.size").description("페이지 크기"),
                                    fieldWithPath("data.totalElements").description("전체 데이터 수"),
                                    fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                    fieldWithPath("data.last").description("마지막 페이지 여부")
                            ))
                    ));
        }
    }

    @Test
    @DisplayName("[내 반품 상태 카운트 조회]")
    void getMyRefundCounts() throws Exception {
        // Given
        Long userId = 1L;
        RefundCountResponse countResponse = new RefundCountResponse(5, 2, 3);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(refundService.getMyRefundCounts(userId)).willReturn(countResponse);

            // When & Then
            mockMvc.perform(get("/users/me/refunds/counts")
                            .header("X-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(5))
                    .andDo(document("refund-my-counts",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(withHeader(
                                    fieldWithPath("data.total").description("전체 반품 신청 건수"),
                                    fieldWithPath("data.process").description("처리 중 건수"),
                                    fieldWithPath("data.approved").description("완료(승인) 건수")
                            ))
                    ));
        }
    }
}