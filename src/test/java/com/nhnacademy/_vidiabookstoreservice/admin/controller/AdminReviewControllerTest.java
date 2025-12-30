package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminReviewResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.impl.AdminReviewServiceImpl;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminReviewController.class)
class AdminReviewControllerTest extends SupportControllerTest {

    @MockitoBean
    private AdminReviewServiceImpl adminReviewService;

    @Test
    @DisplayName("GET - 관리자 리뷰 목록 조회 (검색 + 평점 + 페이징)")
    void getReviewPage() throws Exception {
        AdminReviewResponse reviewResponse = new AdminReviewResponse(
                1L, 100L, "테스트 도서", "user@test.com", "사용자",
                5, "좋은 책입니다.", true, List.of("http://image.url"), LocalDate.now()
        );

        PageResponse<AdminReviewResponse> pageResponse = new PageResponse<>(
                List.of(reviewResponse), 0, 20, 1, 1, true
        );

        given(adminReviewService.getReviews(anyString(), anyInt(), anyInt(), anyInt()))
                .willReturn(pageResponse);

        mockMvc.perform(get("/admin/reviews")
                        .param("keyword", "테스트")
                        .param("rating", "5")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].bookTitle").value("테스트 도서"))
                .andDo(document("admin-reviews-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드 (도서 제목 등)").optional(),
                                parameterWithName("rating").description("평점 필터").optional(),
                                parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 당 수량").optional()
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.content[].id").description("리뷰 ID"),
                                fieldWithPath("data.content[].bookId").description("도서 ID"),
                                fieldWithPath("data.content[].bookTitle").description("도서 제목"),
                                fieldWithPath("data.content[].email").description("작성자 이메일"),
                                fieldWithPath("data.content[].userNickname").description("작성자 닉네임(이름)"),
                                fieldWithPath("data.content[].rating").description("평점"),
                                fieldWithPath("data.content[].content").description("리뷰 내용"),
                                fieldWithPath("data.content[].hasPhoto").description("사진 포함 여부"),
                                fieldWithPath("data.content[].imageUrls[]").description("리뷰 이미지 URL 목록"),
                                fieldWithPath("data.content[].createdAt").description("작성일"),

                                fieldWithPath("data.page").description("현재 페이지 번호"),
                                fieldWithPath("data.size").description("페이지 당 수량"),
                                fieldWithPath("data.totalElements").description("전체 요소 수"),
                                fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.last").description("마지막 페이지 여부")
                        ))
                ));
    }

    @Test
    @DisplayName("DELETE - 관리자 리뷰 삭제")
    void deleteReview() throws Exception {
        Long reviewId = 1L;
        doNothing().when(adminReviewService).deleteReview(reviewId);

        mockMvc.perform(delete("/admin/reviews/{reviewId}", reviewId))
                .andExpect(status().isOk())
                .andDo(document("admin-review-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("reviewId").description("삭제할 리뷰 ID")
                        ),
                        responseFields(withHeader())
                ));
    }
}