package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.request.GradePolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.GradePolicyResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.GradePolicyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GradePolicyController.class)
class GradePolicyControllerTest extends SupportControllerTest {

    @MockitoBean
    private GradePolicyService gradePolicyService;

    @Test
    @DisplayName("GET - 등급 정책 조회 (전체)")
    void getAll() throws Exception {
        GradePolicyResponse policy = new GradePolicyResponse(1L, "PLATINUM", 5);
        given(gradePolicyService.getAll()).willReturn(List.of(policy));

        mockMvc.perform(get("/admin/grade-policies")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].gradeName").value("PLATINUM"))
                .andDo(document("admin-grade-policies-all-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(withHeader(
                                fieldWithPath("data[].gradeId").description("등급 정책 ID"),
                                fieldWithPath("data[].gradeName").description("등급 명칭"),
                                fieldWithPath("data[].pointRate").description("포인트 적립률 (%)")
                        ))
                ));
    }

    @Test
    @DisplayName("GET - 등급 정책 조회 (단일)")
    void getPolicy() throws Exception {
        Long policyId = 1L;
        GradePolicyResponse response = new GradePolicyResponse(policyId, "GOLD", 3);
        given(gradePolicyService.get(policyId)).willReturn(response);

        mockMvc.perform(get("/admin/grade-policies/{policy-id}", policyId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gradeName").value("GOLD"))
                .andDo(document("admin-grade-policies-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("policy-id").description("조회할 등급 정책 ID")
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.gradeId").description("등급 정책 ID"),
                                fieldWithPath("data.gradeName").description("등급 명칭"),
                                fieldWithPath("data.pointRate").description("포인트 적립률 (%)")
                        ))
                ));
    }

    @Test
    @DisplayName("PUT - 등급 정책 수정")
    void update() throws Exception {
        Long gradeId = 1L;
        GradePolicyUpdateRequest request = new GradePolicyUpdateRequest(4);

        doNothing().when(gradePolicyService).update(eq(gradeId), any(GradePolicyUpdateRequest.class));

        mockMvc.perform(put("/admin/grade-policies/{gradeId}", gradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("admin-grade-policies-update-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("gradeId").description("수정할 등급 ID")
                        ),
                        requestFields(
                                fieldWithPath("pointRate").description("변경할 포인트 적립률 (최소 1)")
                        ),
                        responseFields(withHeader())
                ));
    }
}