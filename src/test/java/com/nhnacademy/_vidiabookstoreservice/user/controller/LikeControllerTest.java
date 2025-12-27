package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.StockStatus;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.Like;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.LikeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LikeControllerTest extends SupportControllerTest {

    @Autowired private UserRepository userRepository;
    @Autowired private GradeRepository gradeRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private LikeRepository likeRepository;

    private Long testUserId;
    private Long testBookId;

    @BeforeEach
    void initData() {

        Grade grade = gradeRepository.save(Grade.builder().gradeName(GradeName.WELCOME).pointRate(1).build());

        User user = User.builder()
                .email("like_user@test.com")
                .password("pwd")
                .name("User")
                .phone("01000000000")
                .birthDate(LocalDate.now())
                .grade(grade)
                .build();
        user.setStatus(UserStatus.ACTIVE);
        this.testUserId = userRepository.save(user).getUserId();

        Book book = Book.builder()
                .title("테스트 책")
                .priceStandard(10000)
                .priceSales(9000)
                .packagingAvailable(true)
                .stockStatus(StockStatus.IN_STOCK)
                .build();
        this.testBookId = bookRepository.save(book).getId();

        // 미리 좋아요 하나 등록 (조회/삭제 테스트용)
        likeRepository.save(new Like(user, book));
    }

    @Test
    @DisplayName("[좋아요 리스트 조회(pageable)]")
    void getLikeListPage() throws Exception {
        mockMvc.perform(get("/users/me/likes")
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("user-likes-page-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        responseFields(
                                fieldWithPath("content[].bookId").description("도서 아이디"),
                                fieldWithPath("content[].bookTitle").description("도서 제목"),
                                fieldWithPath("content[].authorName").description("저자"), // Book entity에 저자가 없으면 null일 수 있음
                                fieldWithPath("content[].priceStandard").description("정가"),
                                fieldWithPath("content[].priceSales").description("할인가"),
                                fieldWithPath("content[].stockStatus").description("재고상태"),
                                fieldWithPath("content[].bookImage").description("도서 이미지").optional(),

                                fieldWithPath("page").description("현재 페이지 번호 (0부터 시작)"),
                                fieldWithPath("size").description("페이지 크기"),
                                fieldWithPath("totalElements").description("전체 요소 수"),
                                fieldWithPath("totalPages").description("전체 페이지 수"),
                                fieldWithPath("last").description("마지막 페이지 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[좋아요 리스트 조회(list)]")
    void getLikeList() throws Exception {
        mockMvc.perform(get("/users/me/likes/all")
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("user-likes-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].bookId").description("도서 아이디"),
                                fieldWithPath("[].bookTitle").description("도서 제목"),
                                fieldWithPath("[].authorName").description("저자"), // Book entity에 저자가 없으면 null일 수 있음
                                fieldWithPath("[].priceStandard").description("정가"),
                                fieldWithPath("[].priceSales").description("할인가"),
                                fieldWithPath("[].stockStatus").description("재고상태"),
                                fieldWithPath("[].bookImage").description("도서 이미지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("[좋아요 등록]")
    void addLike() throws Exception {
        // 새로운 책 생성
        Book newBook = bookRepository.save(Book.builder()
                .title("새책")
                .packagingAvailable(true)
                .stockStatus(StockStatus.IN_STOCK)
                .build());

        mockMvc.perform(post("/users/me/likes/{book-id}", newBook.getId())
                        .header("X-User-Id", testUserId))
                .andExpect(status().isCreated())
                .andDo(document("user-like-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        pathParameters(parameterWithName("book-id").description("좋아요 등록할 도서 ID"))
                ));
    }

    @Test
    @DisplayName("[좋아요 삭제]")
    void removeLike() throws Exception {
        // initData 에서 등록한 testBookId 삭제
        mockMvc.perform(delete("/users/me/likes/{book-id}", testBookId)
                        .header("X-User-Id", testUserId))
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
        mockMvc.perform(delete("/users/me/likes")
                        .header("X-User-Id", testUserId))
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