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
        Long userId = 15L;

        mockMvc.perform(get("/users/me/orders")
                        .header("X-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
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


    @Test
    @DisplayName("[주문화면에 필요한 유저 정보 조회]")
    void getOrderInfo() throws Exception {
        Long userId = 14L;

        mockMvc.perform(get("/users/me/order-info")
                        .header("X-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("user-me-order-info-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        responseFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("phone").description("전화번호"),
                                fieldWithPath("point").description("포인트"),
                                fieldWithPath("addressId").description("기본주소 PK"),
                                fieldWithPath("alias").description("별칭"),
                                fieldWithPath("roadAddress").description("도로명 주소"),
                                fieldWithPath("zipCode").description("우편번호"),
                                fieldWithPath("addressDetail").description("상세주소"),

                                fieldWithPath("addressResponses[]").description("주소 리스트"),

                                fieldWithPath("addressResponses[].addressId").description("주소 PK들"),
                                fieldWithPath("addressResponses[].alias").description("별칭들"),
                                fieldWithPath("addressResponses[].roadAddress").description("도로명 주소들"),
                                fieldWithPath("addressResponses[].zipCode").description("우편번호들"),
                                fieldWithPath("addressResponses[].addressDetail").description("상세주소들")
                        )
                ));
    }
}