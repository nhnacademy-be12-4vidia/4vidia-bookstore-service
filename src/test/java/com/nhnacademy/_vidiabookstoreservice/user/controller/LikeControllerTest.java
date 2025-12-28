package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.LikeResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.LikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LikeControllerTest extends SupportControllerTest {

    @MockitoBean
    private LikeService likeService;

    @Test
    @DisplayName("[좋아요 페이징 조회]")
    void getLikeListPage() throws Exception {
        // Given
        Long userId = 1L;
        LikeResponse response = new LikeResponse(100L, "테스트 도서", "테스트 저자", 20000, 18000, "IN_STOCK", "/img/test.png");
        PageResponse<LikeResponse> pageResponse = new PageResponse<>(List.of(response), 0, 10, 1, 1, true);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(likeService.getLikesPage(eq(userId), any(Pageable.class))).willReturn(pageResponse);

            // When & Then
            mockMvc.perform(get("/users/me/likes")
                            .header("X-User-Id", userId)
                            .param("page", "0")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].bookTitle").value("테스트 도서"))
                    .andDo(document("like-get-page",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(headerWithName("X-User-Id").description("회원 식별 ID")),
                            queryParameters(
                                    parameterWithName("page").description("페이지 번호").optional(),
                                    parameterWithName("size").description("페이지 크기").optional()
                            ),
                            responseFields(withHeader(
                                    fieldWithPath("data.content[].bookId").description("도서 ID"),
                                    fieldWithPath("data.content[].bookTitle").description("도서 제목"),
                                    fieldWithPath("data.content[].authorName").description("저자 이름"),
                                    fieldWithPath("data.content[].priceStandard").description("정가"),
                                    fieldWithPath("data.content[].priceSales").description("판매가"),
                                    fieldWithPath("data.content[].stockStatus").description("재고 상태"),
                                    fieldWithPath("data.content[].bookImage").description("도서 이미지 URL"),
                                    fieldWithPath("data.page").description("현재 페이지"),
                                    fieldWithPath("data.size").description("페이지 당 수량"),
                                    fieldWithPath("data.totalElements").description("총 요소 수"),
                                    fieldWithPath("data.totalPages").description("총 페이지 수"),
                                    fieldWithPath("data.last").description("마지막 페이지 여부")
                            ))
                    ));
        }
    }

    @Test
    @DisplayName("[좋아요 전체 조회]")
    void getLikeList() throws Exception {
        // Given
        Long userId = 1L;
        LikeResponse response = new LikeResponse(100L, "전체 도서", "저자", 10000, 9000, "IN_STOCK", "/img/all.png");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(likeService.getLikes(userId)).willReturn(List.of(response));

            mockMvc.perform(get("/users/me/likes/all")
                            .header("X-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].bookTitle").value("전체 도서"))
                    .andDo(document("like-get-all",
                            responseFields(withHeader(
                                    fieldWithPath("data[].bookId").description("도서 ID"),
                                    fieldWithPath("data[].bookTitle").description("도서 제목"),
                                    fieldWithPath("data[].authorName").description("저자 이름"),
                                    fieldWithPath("data[].priceStandard").description("정가"),
                                    fieldWithPath("data[].priceSales").description("판매가"),
                                    fieldWithPath("data[].stockStatus").description("재고 상태"),
                                    fieldWithPath("data[].bookImage").description("도서 이미지 URL")
                            ))
                    ));
        }
    }

    @Test
    @DisplayName("[좋아요 등록]")
    void addLike() throws Exception {
        Long userId = 1L;
        Long bookId = 100L;

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            doNothing().when(likeService).addLike(userId, bookId);

            mockMvc.perform(post("/users/me/likes/{book-id}", bookId)
                            .header("X-User-Id", userId))
                    .andExpect(status().isCreated())
                    .andDo(document("like-add-post",
                            pathParameters(parameterWithName("book-id").description("좋아요 등록할 도서 ID")),
                            responseFields(withHeader())
                    ));
        }
    }

    @Test
    @DisplayName("[좋아요 삭제]")
    void removeLike() throws Exception {
        Long userId = 1L;
        Long bookId = 100L;

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            doNothing().when(likeService).removeLike(userId, bookId);

            mockMvc.perform(delete("/users/me/likes/{book-id}", bookId)
                            .header("X-User-Id", userId))
                    .andExpect(status().isOk())
                    .andDo(document("like-remove-delete",
                            pathParameters(parameterWithName("book-id").description("좋아요 삭제할 도서 ID")),
                            responseFields(withHeader())
                    ));
        }
    }

    @Test
    @DisplayName("[좋아요 전체 삭제]")
    void removeAllLike() throws Exception {
        Long userId = 1L;
        LikeResponse response = new LikeResponse(100L, "삭제용", "저자", 1000, 900, "IN_STOCK", "/img.png");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(likeService.getLikes(userId)).willReturn(List.of(response));
            doNothing().when(likeService).removeAllLike(eq(userId), anyList());

            mockMvc.perform(delete("/users/me/likes")
                            .header("X-User-Id", userId))
                    .andExpect(status().isOk())
                    .andDo(document("like-remove-all-delete",
                            responseFields(withHeader())
                    ));
        }
    }
}