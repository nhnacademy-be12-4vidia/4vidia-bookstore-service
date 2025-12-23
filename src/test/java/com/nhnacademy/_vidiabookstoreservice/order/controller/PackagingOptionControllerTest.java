package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.PackagingOptionRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.PackagingOptionResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.PackagingOptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PackagingOptionControllerTest extends SupportControllerTest {

    @MockitoBean private PackagingOptionService packagingOptionService;

    @BeforeEach
    void initData() { }

    @Test
    @DisplayName("[포장 옵션 생성]")
    void createPackagingOption() throws Exception {
        PackagingOptionRequest request = new PackagingOptionRequest(
                "선물용 포장",
                3000
        );

        // when & then
        mockMvc.perform(post("/packaging-options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(document("order-packaging-option-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("name").description("포장 옵션 이름"),
                                fieldWithPath("price").description("포장 옵션 가격")
                        )
                ));

        // verify: 서비스 메소드 호출 확인
        verify(packagingOptionService).savePackagingOption(any(PackagingOptionRequest.class));
    }

    @Test
    @DisplayName("[포장 옵션 목록 조회]")
    void getAllPackagingOptions() throws Exception {
        List<PackagingOptionResponse> responses = List.of(
                new PackagingOptionResponse(1L, "일반 포장", 1000),
                new PackagingOptionResponse(2L, "고급 선물 포장", 3000)
        );

        given(packagingOptionService.getPackagingOptions()).willReturn(responses);

        // when & then
        mockMvc.perform(get("/packaging-options")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("order-packaging-option-list-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("[]").description("포장 옵션 목록"),
                                fieldWithPath("[].packagingOptionId").description("포장 옵션 ID (PK)"),
                                fieldWithPath("[].name").description("포장 옵션 이름"),
                                fieldWithPath("[].price").description("포장 옵션 가격")
                        )
                ));
    }
}