package com.nhnacademy._vidiabookstoreservice.book.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.response.CategoryListResponse;
import com.nhnacademy._vidiabookstoreservice.book.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerTest extends SupportControllerTest {

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 목록 조회")
    void getCategoryList() throws Exception {
        // Given
        List<CategoryListResponse> responses = List.of(
                CategoryListResponse.builder()
                        .id(1L)
                        .kdcCode("800")
                        .categoryName("문학")
                        .depth(1) // 테스트 데이터 추가
                        .build(),
                CategoryListResponse.builder()
                        .id(2L)
                        .kdcCode("000")
                        .categoryName("총류")
                        .depth(1) // 테스트 데이터 추가
                        .build()
        );

        given(categoryService.getCategoryList()).willReturn(responses);

        // When & Then
        mockMvc.perform(get("/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].categoryName").value("문학"))
                .andExpect(jsonPath("$.data[0].kdcCode").value("800"))
                .andDo(document("category-list-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(withHeader(
                                fieldWithPath("data[].id").description("카테고리 ID"),
                                fieldWithPath("data[].kdcCode").description("KDC 코드"),
                                fieldWithPath("data[].categoryName").description("카테고리 이름"),
                                fieldWithPath("data[].depth").description("카테고리 깊이") // 필드 문서화 추가
                        ))
                ));
    }

    @Test
    @DisplayName("평탄화된 카테고리 목록 조회 (Flat)")
    void getFlatCategoryList() throws Exception {
        // Given
        List<CategoryListResponse> responses = List.of(
                CategoryListResponse.builder()
                        .id(3L)
                        .kdcCode("810")
                        .categoryName("한국문학")
                        .depth(2) // 테스트 데이터 추가
                        .build()
        );

        given(categoryService.getFlatCategoryList()).willReturn(responses);

        // When & Then
        mockMvc.perform(get("/categories/flat")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].categoryName").value("한국문학"))
                .andDo(document("category-flat-list-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(withHeader(
                                fieldWithPath("data[].id").description("카테고리 ID"),
                                fieldWithPath("data[].kdcCode").description("KDC 코드"),
                                fieldWithPath("data[].categoryName").description("카테고리 이름"),
                                fieldWithPath("data[].depth").description("카테고리 깊이") // 필드 문서화 추가
                        ))
                ));
    }
}