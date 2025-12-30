package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.request.PointPolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.PointPolicyResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.impl.PointPolicyServiceImpl;
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

@WebMvcTest(PointPolicyController.class)
class PointPolicyControllerTest extends SupportControllerTest {

    @MockitoBean
    private PointPolicyServiceImpl pointPolicyService;

    @Test
    @DisplayName("GET - 포인트 정책 전체 조회")
    void getAll() throws Exception {
        PointPolicyResponse policy = new PointPolicyResponse(1L, "LOGIN", 500);
        given(pointPolicyService.getAll()).willReturn(List.of(policy));

        mockMvc.perform(get("/admin/point-policies")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].pointName").value("LOGIN"))
                .andDo(document("admin-point-policies-all-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(withHeader(
                                fieldWithPath("data[].pointPolicyId").description("포인트 정책 ID"),
                                fieldWithPath("data[].pointName").description("포인트 이름"),
                                fieldWithPath("data[].price").description("금액")
                        ))
                ));
    }

    @Test
    @DisplayName("GET - 특정 포인트 정책 조회")
    void getPolicy() throws Exception {
        Long policyId = 1L;
        PointPolicyResponse response = new PointPolicyResponse(policyId, "REGISTER", 1000);
        given(pointPolicyService.get(policyId)).willReturn(response);

        mockMvc.perform(get("/admin/point-policies/{policy-id}", policyId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pointName").value("REGISTER"))
                .andDo(document("admin-point-policies-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("policy-id").description("조회할 포인트 정책 ID")
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.pointPolicyId").description("포인트 정책 ID"),
                                fieldWithPath("data.pointName").description("포인트 이름"),
                                fieldWithPath("data.price").description("금액")
                        ))
                ));
    }

    @Test
    @DisplayName("PUT - 포인트 정책 수정")
    void update() throws Exception {
        Long policyId = 1L;
        PointPolicyUpdateRequest request = new PointPolicyUpdateRequest(2000);

        doNothing().when(pointPolicyService).update(eq(policyId), any(PointPolicyUpdateRequest.class));

        mockMvc.perform(put("/admin/point-policies/{policy-id}", policyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // Controller에서 HttpStatus.CREATED 반환 확인
                .andDo(document("admin-point-policies-update-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("policy-id").description("수정할 포인트 정책 ID")
                        ),
                        requestFields(
                                fieldWithPath("price").description("변경할 포인트 금액")
                        ),
                        responseFields(withHeader())
                ));
    }
}