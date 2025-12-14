package com.nhnacademy._vidiabookstoreservice.user.controller;

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

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@ActiveProfiles("local")
@SpringBootTest
@Transactional
class MyOrderControllerTest {

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
    @DisplayName("[주문내역 미리보기]")
    void getOrderPreview() throws Exception {
        Long userId = 71L; // docs@naver.com (다른 회원은 테스트하느라 주문내역이 너무 많음)

        mockMvc.perform(get("/users/me/orders")
                        .header("X-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(document("user-me-orders-get",
                        preprocessRequest(prettyPrint()), // 요청/응답 body를 보기 좋게 출력해준데요 (없으면 한줄로 출력)
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        responseFields( // 리스트일때, []. 을 붙이래요
                                fieldWithPath("[].orderId").description("주문 아이디"),
                                fieldWithPath("[].userId").description("유저 아이디"),
                                fieldWithPath("[].createdAt").description("주문 생성일"),
                                fieldWithPath("[].deliveryStatus").description("배송상태"),

                                // 중첩 리스트
                                fieldWithPath("[].orderItems").description("주문 도서 목록"),

                                fieldWithPath("[].orderItems[].orderItemId").description("주문 상품 아이디"),
                                fieldWithPath("[].orderItems[].bookId").description("도서 아이디"),
                                fieldWithPath("[].orderItems[].bookTitle").description("도서 제목"),
                                fieldWithPath("[].orderItems[].bookAuthor").description("도서 저자"),
                                fieldWithPath("[].orderItems[].bookImageUrl").description("도서 이미지").optional(), // image 없으면 null반환
                                fieldWithPath("[].orderItems[].quantity").description("주문 수량"),
                                fieldWithPath("[].orderItems[].salePrice").description("구매 당시 가격"), // ?
                                fieldWithPath("[].orderItems[].confirmStatus").description("주문 확정 상태"),
                                fieldWithPath("[].orderItems[].isReviewed").description("리뷰 작성 여부")
                        )
                ));
    }
}