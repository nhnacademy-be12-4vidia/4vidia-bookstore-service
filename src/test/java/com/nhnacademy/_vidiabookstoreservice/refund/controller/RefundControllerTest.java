package com.nhnacademy._vidiabookstoreservice.refund.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
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
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RefundControllerTest extends SupportControllerTest {

    @MockitoBean
    private RefundService refundService;

    private final Long testUserId = 1L;

    @Test
    @DisplayName("[반품 신청 가능한 도서 리스트 조회]")
    void getRefundList() throws Exception {
        Long orderId = 100L;
        OrderItemResponse item = new OrderItemResponse(1L, 10L, "Test Book", 2);
        RefundResponse response = new RefundResponse(orderId, List.of(item));

        given(refundService.getRefundList(orderId)).willReturn(response);

        mockMvc.perform(get("/orders/{order-id}/refunds", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("refund-list-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        pathParameters(
                                parameterWithName("order-id").description("주문 ID")
                        ),
                        responseFields(
                                fieldWithPath("orderId").description("주문 ID"),
                                fieldWithPath("orderItems").description("주문 상품 목록"),
                                fieldWithPath("orderItems[].orderItemId").description("주문 상품 ID"),
                                fieldWithPath("orderItems[].bookId").description("도서 ID"),
                                fieldWithPath("orderItems[].bookTitle").description("도서 제목"),
                                fieldWithPath("orderItems[].quantity").description("주문 수량")
                        )
                ));
    }

    @Test
    @DisplayName("[반품 신청서 작성]")
    void refundRegister() throws Exception {
        RefundRequest request = new RefundRequest(
                100L,
                "Damaged item",
                true,
                List.of(1L, 2L)
        );

        willDoNothing().given(refundService).refundRegister(any(RefundRequest.class));

        mockMvc.perform(post("/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("refund-register-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("orderId").description("주문 ID"),
                                fieldWithPath("reason").description("반품 사유"),
                                fieldWithPath("damaged").description("파손 여부"),
                                fieldWithPath("orderItemIds").description("반품할 주문 상품 ID 목록")
                        )
                ));
    }

    @Test
    @DisplayName("[반품 신청 내역 조회]")
    void getMyRefundHistories() throws Exception {
        RefundHistoryGroupResponse.RefundItemResponse itemResponse = new RefundHistoryGroupResponse.RefundItemResponse(
                "Test Book",
                1,
                10000,
                RefundItemStatus.PROCESS,
                null
        );

        RefundHistoryGroupResponse groupResponse = new RefundHistoryGroupResponse(
                1L,
                100L,
                LocalDate.now().minusDays(5),
                LocalDate.now(),
                RefundStatus.PROCESS,
                "Simple change of mind",
                10000,
                List.of(itemResponse)
        );

        Page<RefundHistoryGroupResponse> page = new PageImpl<>(List.of(groupResponse), PageRequest.of(0, 10), 1);

        given(refundService.getMyRefunds(eq(testUserId), any(), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/users/me/refunds")
                        .header("X-User-Id", testUserId)
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "PROCESS")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("refund-history-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("회원 ID")
                        ),
                        queryParameters(
                                parameterWithName("status").description("반품 상태 필터 (PROCESS, APPROVED 등)").optional(),
                                parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        ),
                        responseFields(
                                fieldWithPath("content[].refundId").description("반품 ID"),
                                fieldWithPath("content[].orderId").description("주문 ID"),
                                fieldWithPath("content[].orderDate").description("주문 일자"),
                                fieldWithPath("content[].refundDate").description("반품 신청 일자"),
                                fieldWithPath("content[].refundStatus").description("반품 상태"),
                                fieldWithPath("content[].refundReason").description("반품 사유"),
                                fieldWithPath("content[].totalRefundPrice").description("총 환불 예정 금액"),
                                fieldWithPath("content[].items").description("반품 아이템 목록"),
                                fieldWithPath("content[].items[].title").description("도서 제목"),
                                fieldWithPath("content[].items[].quantity").description("수량"),
                                fieldWithPath("content[].items[].price").description("환불 금액"),
                                fieldWithPath("content[].items[].refundItemStatus").description("아이템별 반품 상태"),
                                fieldWithPath("content[].items[].rejectDetail").description("거절 사유").optional(),

                                fieldWithPath("page").description("현재 페이지"),
                                fieldWithPath("size").description("페이지 크기"),
                                fieldWithPath("totalElements").description("전체 데이터 수"),
                                fieldWithPath("totalPages").description("전체 페이지 수"),
                                fieldWithPath("last").description("마지막 페이지 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[반품 내역 카운트 조회]")
    void getMyRefundCounts() throws Exception {
        RefundCountResponse response = new RefundCountResponse(10, 5, 5);

        given(refundService.getMyRefundCounts(testUserId)).willReturn(response);

        mockMvc.perform(get("/users/me/refunds/counts")
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("refund-counts-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("회원 ID")
                        ),
                        responseFields(
                                fieldWithPath("total").description("전체 반품 수"),
                                fieldWithPath("process").description("처리 중인 반품 수"),
                                fieldWithPath("approved").description("승인된 반품 수")
                        )
                ));
    }
}