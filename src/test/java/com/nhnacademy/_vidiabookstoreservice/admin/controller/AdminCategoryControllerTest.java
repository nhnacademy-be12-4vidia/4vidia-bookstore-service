package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.request.CreateCategoryRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.request.UpdateCategoryRequest;
import com.nhnacademy._vidiabookstoreservice.book.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCategoryController.class)
class AdminCategoryControllerTest extends SupportControllerTest {

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @DisplayName("POST - 카테고리 생성")
    void createCategory() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest("800", "문학");

        doNothing().when(categoryService).createCategory(any(CreateCategoryRequest.class));

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("admin-category-create-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("kdcCode").description("KDC 코드 (3자리 숫자 또는 대문자)"),
                                fieldWithPath("categoryName").description("카테고리 명칭")
                        ),
                        responseFields(withHeader())
                ));
    }

    @Test
    @DisplayName("PUT - 카테고리 수정")
    void updateCategory() throws Exception {
        Long categoryId = 1L;
        UpdateCategoryRequest request = new UpdateCategoryRequest("수정된 카테고리");

        doNothing().when(categoryService).updateCategory(eq(categoryId), any(UpdateCategoryRequest.class));

        mockMvc.perform(put("/admin/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("admin-category-update-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("categoryId").description("수정할 카테고리 ID")
                        ),
                        requestFields(
                                fieldWithPath("categoryName").description("변경할 카테고리 명칭")
                        ),
                        responseFields(withHeader())
                ));
    }

    @Test
    @DisplayName("DELETE - 카테고리 삭제")
    void deleteCategory() throws Exception {
        Long categoryId = 1L;
        doNothing().when(categoryService).deleteCategory(categoryId);

        mockMvc.perform(delete("/admin/categories/{categoryId}", categoryId))
                .andExpect(status().isOk())
                .andDo(document("admin-category-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("categoryId").description("삭제할 카테고리 ID")
                        ),
                        responseFields(withHeader())
                ));
    }
}