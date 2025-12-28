package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.PackagingOptionRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.PackagingOptionResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.PackagingOptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PackagingOptionControllerTest extends SupportControllerTest {

    @MockitoBean
    private PackagingOptionService packagingOptionService;

    @Test
    @DisplayName("[포장 옵션 생성]")
    void createPackagingOption() throws Exception {
        // Given
        PackagingOptionRequest request = new PackagingOptionRequest("선물용 고급 포장", 2000);

        given(packagingOptionService.savePackagingOption(any(PackagingOptionRequest.class))).willReturn(null);

        // When & Then
        mockMvc.perform(post("/packaging-options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("packaging-option-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description("포장 옵션 이름"),
                                fieldWithPath("price").description("포장 가격")
                        ),
                        responseFields(withHeader())
                ));
    }

    @Test
    @DisplayName("[전체 포장 옵션 조회]")
    void getAllPackagingOptions() throws Exception {
        // Given
        List<PackagingOptionResponse> responses = List.of(
                new PackagingOptionResponse(1L, "기본 포장", 0),
                new PackagingOptionResponse(2L, "선물 박스", 1000)
        );

        given(packagingOptionService.getPackagingOptions()).willReturn(responses);

        // When & Then
        mockMvc.perform(get("/packaging-options")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("기본 포장"))
                .andDo(document("packaging-option-get-all",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(withHeader(
                                fieldWithPath("data[].packagingOptionId").description("포장 옵션 ID"),
                                fieldWithPath("data[].name").description("포장 옵션 이름"),
                                fieldWithPath("data[].price").description("포장 가격")
                        ))
                ));
    }
}