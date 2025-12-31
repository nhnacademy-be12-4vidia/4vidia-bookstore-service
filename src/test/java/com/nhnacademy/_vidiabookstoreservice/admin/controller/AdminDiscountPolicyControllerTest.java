package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.DiscountPolicyCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.DiscountPolicyResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.DiscountPolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.book.service.DiscountPolicyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminDiscountPolicyController.class)
class AdminDiscountPolicyControllerTest extends SupportControllerTest {

    @MockitoBean
    private DiscountPolicyService discountPolicyService;

    @Test
    @DisplayName("GET - 할인 정책 전체 조회")
    void getPolicies() throws Exception {
        DiscountPolicyResponse response = new DiscountPolicyResponse(
                1L, 10L, "국내도서", "100", "신년 할인", 10,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );
        given(discountPolicyService.getPolicies(any())).willReturn(List.of(response));

        mockMvc.perform(get("/admin/books/discount-policies")
                        .param("categoryId", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].discountPolicyName").value("신년 할인"))
                .andDo(document("admin-discount-policies-all-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("categoryId").description("카테고리 ID 필터 (선택)").optional()
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data[].id").description("정책 ID"),
                                fieldWithPath("data[].categoryId").description("카테고리 ID (기본 정책은 null)").optional(),
                                fieldWithPath("data[].categoryName").description("카테고리명").optional(),
                                fieldWithPath("data[].kdcCode").description("KDC 코드").optional(),
                                fieldWithPath("data[].discountPolicyName").description("정책명"),
                                fieldWithPath("data[].discountRate").description("할인율 (%)"),
                                fieldWithPath("data[].startDate").description("시작일"),
                                fieldWithPath("data[].endDate").description("종료일")
                        ))
                ));
    }

    @Test
    @DisplayName("GET - 할인 정책 상세 조회")
    void getPolicy() throws Exception {
        Long policyId = 1L;
        DiscountPolicyResponse response = new DiscountPolicyResponse(
                policyId, null, null, null, "기본 할인", 5,
                LocalDate.of(2024, 1, 1), LocalDate.of(2099, 12, 31)
        );
        given(discountPolicyService.getPolicy(policyId)).willReturn(response);

        mockMvc.perform(get("/admin/books/discount-policies/{id}", policyId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.discountPolicyName").value("기본 할인"))
                .andDo(document("admin-discount-policy-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("할인 정책 ID")
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.id").description("정책 ID"),
                                fieldWithPath("data.categoryId").description("카테고리 ID").optional(),
                                fieldWithPath("data.categoryName").description("카테고리명").optional(),
                                fieldWithPath("data.kdcCode").description("KDC 코드").optional(),
                                fieldWithPath("data.discountPolicyName").description("정책명"),
                                fieldWithPath("data.discountRate").description("할인율 (%)"),
                                fieldWithPath("data.startDate").description("시작일"),
                                fieldWithPath("data.endDate").description("종료일")
                        ))
                ));
    }

    @Test
    @DisplayName("POST - 할인 정책 생성")
    void createPolicy() throws Exception {
        DiscountPolicyCreateRequest request = new DiscountPolicyCreateRequest();
        request.setCategoryId(10L);
        request.setDiscountPolicyName("새로운 할인");
        request.setDiscountRate(15);
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(LocalDate.of(2024, 1, 31));

        mockMvc.perform(post("/admin/books/discount-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("admin-discount-policy-create-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("categoryId").description("카테고리 ID (기본 정책 시 null)"),
                                fieldWithPath("discountPolicyName").description("정책명"),
                                fieldWithPath("discountRate").description("할인율 (0~100)"),
                                fieldWithPath("startDate").description("시작일"),
                                fieldWithPath("endDate").description("종료일")
                        ),
                        responseFields(withHeader())
                ));
    }

    @Test
    @DisplayName("PUT - 할인 정책 수정")
    void updatePolicy() throws Exception {
        Long policyId = 1L;
        DiscountPolicyUpdateRequest request = new DiscountPolicyUpdateRequest();
        request.setDiscountPolicyName("수정된 정책");
        request.setDiscountRate(20);
        request.setStartDate(LocalDate.of(2024, 2, 1));
        request.setEndDate(LocalDate.of(2024, 2, 28));

        mockMvc.perform(put("/admin/books/discount-policies/{id}", policyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("admin-discount-policy-update-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("수정할 정책 ID")
                        ),
                        requestFields(
                                fieldWithPath("discountPolicyName").description("변경할 정책명"),
                                fieldWithPath("discountRate").description("변경할 할인율"),
                                fieldWithPath("startDate").description("변경할 시작일"),
                                fieldWithPath("endDate").description("변경할 종료일")
                        ),
                        responseFields(withHeader())
                ));
    }

    @Test
    @DisplayName("DELETE - 할인 정책 삭제")
    void deletePolicy() throws Exception {
        Long policyId = 1L;
        doNothing().when(discountPolicyService).deletePolicy(policyId);

        mockMvc.perform(delete("/admin/books/discount-policies/{id}", policyId))
                .andExpect(status().isOk())
                .andDo(document("admin-discount-policy-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("삭제할 정책 ID")
                        ),
                        responseFields(withHeader())
                ));
    }
}