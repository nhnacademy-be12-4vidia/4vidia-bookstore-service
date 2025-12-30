package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.AdminRefundListResponse;
import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.RefundDetailResponse;
import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.RefundItemDto;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminRefundService;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundItemStatus;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundItemUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRefundController.class)
class AdminRefundControllerTest extends SupportControllerTest {

    @MockitoBean
    private AdminRefundService adminRefundService;

    @Test
    @DisplayName("GET - 반품 신청 목록 조회 (페이징)")
    void listByRefundStatus() throws Exception {
        AdminRefundListResponse refundResponse = new AdminRefundListResponse(
                1L, 100L, "user@example.com", "홍길동", LocalDateTime.now(), "PROCESS"
        );
        Page<AdminRefundListResponse> page = new PageImpl<>(List.of(refundResponse), PageRequest.of(0, 20), 1);

        given(adminRefundService.listByRefundStatus(any(RefundStatus.class), anyString(), any(Pageable.class)))
                .willReturn(page);

        mockMvc.perform(get("/admin/refunds")
                        .param("refundStatus", "PROCESS")
                        .param("keyword", "홍길동")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].refundId").value(1L))
                .andDo(document("admin-refund-list-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("refundStatus").description("반품 상태 (PROCESS, APPROVED)").optional(),
                                parameterWithName("keyword").description("검색어 (이메일 또는 이름)").optional(),
                                parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 당 수량").optional()
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.content[].refundId").description("반품 ID"),
                                fieldWithPath("data.content[].orderId").description("주문 ID"),
                                fieldWithPath("data.content[].email").description("사용자 이메일"),
                                fieldWithPath("data.content[].name").description("사용자 이름"),
                                fieldWithPath("data.content[].createdAt").description("반품 신청 일시"),
                                fieldWithPath("data.content[].refundStatus").description("반품 상태"),

                                fieldWithPath("data.page").description("현재 페이지 번호"),
                                fieldWithPath("data.size").description("페이지 당 수량"),
                                fieldWithPath("data.totalElements").description("전체 데이터 수"),
                                fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.last").description("마지막 페이지 여부")
                        ))
                ));
    }

    @Test
    @DisplayName("GET - 반품 상세 정보 조회")
    void getRefundDetail() throws Exception {
        Long refundId = 1L;
        RefundItemDto itemDto = new RefundItemDto(10L, RefundItemStatus.PROCESS, "테스트 도서", "http://image.url", 1, 15000L);
        RefundDetailResponse detail = new RefundDetailResponse(
                refundId, 100L, "user@example.com", "홍길동", "파손되었습니다.",
                LocalDateTime.now(), RefundStatus.PROCESS, List.of(itemDto)
        );

        given(adminRefundService.getRefundDetail(refundId)).willReturn(detail);

        mockMvc.perform(get("/admin/refunds/{refund-id}", refundId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.refundId").value(refundId))
                .andDo(document("admin-refund-detail-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("refund-id").description("조회할 반품 ID")
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.refundId").description("반품 ID"),
                                fieldWithPath("data.orderId").description("주문 ID"),
                                fieldWithPath("data.email").description("사용자 이메일"),
                                fieldWithPath("data.name").description("사용자 이름"),
                                fieldWithPath("data.description").description("반품 사유/상세"),
                                fieldWithPath("data.createdAt").description("반품 신청 일시"),
                                fieldWithPath("data.refundStatus").description("반품 총 상태"),
                                fieldWithPath("data.items[]").description("반품 품목 목록"),
                                fieldWithPath("data.items[].refundItemId").description("반품 품목 ID"),
                                fieldWithPath("data.items[].refundItemStatus").description("품목별 반품 상태"),
                                fieldWithPath("data.items[].bookTitle").description("도서 제목"),
                                fieldWithPath("data.items[].bookImgUrl").description("도서 이미지 URL"),
                                fieldWithPath("data.items[].quantity").description("반품 수량"),
                                fieldWithPath("data.items[].salePrice").description("판매가")
                        ))
                ));
    }

    @Test
    @DisplayName("PUT - 반품 품목 상태 수정 (승인/거절)")
    void updateRefund() throws Exception {
        Long refundItemId = 10L;
        RefundItemUpdateRequest request = new RefundItemUpdateRequest(RefundItemStatus.APPROVED, null);

        doNothing().when(adminRefundService).updateRefundStatus(eq(refundItemId), any(RefundItemUpdateRequest.class));

        mockMvc.perform(put("/admin/refunds/{refund-item-id}", refundItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("admin-refund-status-update-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("refund-item-id").description("상태를 수정할 반품 품목 ID")
                        ),
                        requestFields(
                                fieldWithPath("refundItemStatus").description("변경할 상태 (APPROVED, REJECTED)"),
                                fieldWithPath("rejectDetail").description("거절 사유 (거절 시 필수)").optional()
                        ),
                        responseFields(withHeader())
                ));
    }
}