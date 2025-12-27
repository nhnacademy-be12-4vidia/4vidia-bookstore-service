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
    @DisplayName("[카테고리 전체 목록 조회]")
    void getCategoryList() throws Exception {
        List<CategoryListResponse> responses = List.of(
                CategoryListResponse.builder()
                        .id(1L)
                        .kdcCode("000")
                        .categoryName("총류")
                        .build(),
                CategoryListResponse.builder()
                        .id(2L)
                        .kdcCode("800")
                        .categoryName("문학")
                        .build()
        );

        given(categoryService.getCategoryList()).willReturn(responses);

        mockMvc.perform(get("/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].categoryName").value("총류"))
                .andDo(document("book-category-list-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].id").description("카테고리 ID"),
                                fieldWithPath("[].kdcCode").description("KDC 코드"),
                                fieldWithPath("[].categoryName").description("카테고리 이름")
                        )
                ));
    }

    @Test
    @DisplayName("[카테고리 플랫 목록 조회]")
    void getFlatCategoryList() throws Exception {
        List<CategoryListResponse> responses = List.of(
                CategoryListResponse.builder()
                        .id(10L)
                        .kdcCode("810")
                        .categoryName("한국문학")
                        .build(),
                CategoryListResponse.builder()
                        .id(11L)
                        .kdcCode("820")
                        .categoryName("영미문학")
                        .build()
        );

        given(categoryService.getFlatCategoryList()).willReturn(responses);

        mockMvc.perform(get("/categories/flat")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(10L))
                .andDo(document("book-category-flat-list-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].id").description("카테고리 ID"),
                                fieldWithPath("[].kdcCode").description("KDC 코드"),
                                fieldWithPath("[].categoryName").description("카테고리 이름")
                        )
                ));
    }
}