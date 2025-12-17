package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.LikeResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.impl.LikeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@ActiveProfiles("local")
@SpringBootTest
@Transactional
class LikeControllerTest {

    @Autowired
    private BookService bookService;
    @Autowired
    private LikeServiceImpl likeServiceImpl;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }


    @Test
    @DisplayName("[좋아요 리스트 조회]")
    void getLikeList() throws Exception {
        Long userId = 14L; // user1@naver.com

        mockMvc.perform(get("/users/me/likes")
                        .header("X-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("user-likes-get",
                        preprocessRequest(prettyPrint()), // 요청/응답 body를 보기 좋게 출력해준데요 (없으면 한줄로 출력)
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        responseFields( // 리스트일때, []. 을 붙이래요
                                fieldWithPath("[].bookId").description("도서 아이디"),
                                fieldWithPath("[].bookTitle").description("도서 제목"),
                                fieldWithPath("[].authorName").description("저자"),
                                fieldWithPath("[].priceStandard").description("정가"),
                                fieldWithPath("[].priceSales").description("할인가"),
                                fieldWithPath("[].stockStatus").description("재고상태"),
                                fieldWithPath("[].bookImage").description("도서 이미지")
                        )
                ));
    }

    @Test
    @DisplayName("[좋아요 등록]")
    void addLike() throws Exception {
        Long userId = 14L; // user1@naver.com
        Long targetBookId = bookService.getProxyById(29120L).getId(); // 임의의 도서

        mockMvc.perform(post("/users/me/likes/{book-id}", targetBookId)
                        .header("X-User-Id", userId))
                .andExpect(status().isCreated())
                .andDo(document("user-like-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        pathParameters(
                                parameterWithName("book-id").description("좋아요 등록할 도서 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[좋아요 삭제]")
    void removeLike() throws Exception {
        Long userId = 14L; // user1@naver.com

        List<LikeResponse> likes = likeServiceImpl.getLikes(userId); // ㅇ?? 이상한데
        Long targetBookId = likes.stream().findFirst().get().bookId(); // userId에 해당하는 유저가 실제 db에 좋아요 등록해놓은게 없으면???

        mockMvc.perform(delete("/users/me/likes/{book-id}", targetBookId)
                        .header("X-User-Id", userId))
                .andExpect(status().isNoContent())
                .andDo(document("user-like-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        pathParameters(
                                parameterWithName("book-id").description("좋아요 삭제할 도서 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[좋아요 전체 삭제]")
    void removeAllLike() throws Exception {
        Long userId = 14L; // user1@naver.com

        mockMvc.perform(delete("/users/me/likes")
                        .header("X-User-Id", userId))
                .andExpect(status().isNoContent())
                .andDo(document("user-likes-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        )
                ));
    }
}