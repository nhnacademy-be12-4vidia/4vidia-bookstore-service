package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutListRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.DeliveryDateResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderBookResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderCheckoutResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.PackagingOptionResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderCheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderCheckoutControllerTest extends SupportControllerTest {

    @MockitoBean
    private OrderCheckoutService orderCheckoutService;

    @BeforeEach
    void initData() { }

    @Test
    @DisplayName("[주문 화면에 보여줄 값 조회]")
    void getOrderCheckout() throws Exception {
        // todo: 테스트 데이터 수정 (key값, response 데이터)
        Long userId = 1L;
        String key = "redis-key";

        OrderCheckoutResponse mockResponse = new OrderCheckoutResponse(
                "주문 이름 예시",
                20000,
                List.of(new OrderBookResponse(
                        1L,
                        "책 제목",
                        "저자",
                        "이미지URL",
                        "KDC",
                        2,
                        10000
                )),
                List.of(new DeliveryDateResponse(
                        "?",
                        "오늘 도착"
                )),
                List.of(new PackagingOptionResponse(
                        1L,
                        "선물 포장",
                        1000
                ))
        );

        given(orderCheckoutService.getOrderCheckoutResponse(anyLong(), anyString()))
                .willReturn(mockResponse);

        mockMvc.perform(get("/orders")
                        .header("X-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("key", key)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("order-checkout-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        // todo: 데이터 설명 수정 필요
                        queryParameters(
                                parameterWithName("key").description("Redis에서 꺼낼 키값")
                        ),
                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        responseFields(
                                fieldWithPath("orderName").description("주문 이름"),
                                fieldWithPath("finalAmount").description("최종 가격"),

                                fieldWithPath("bookItems").description("도서 아이템 리스트"),
                                fieldWithPath("bookItems[].bookId").description("도서 PK"),
                                fieldWithPath("bookItems[].bookTitle").description("제목"),
                                fieldWithPath("bookItems[].bookAuthor").description("저자"),
                                fieldWithPath("bookItems[].bookImageUrl").description("이미지 url"),
                                fieldWithPath("bookItems[].categoryKdc").description("카테코리 kdc 코드"),
                                fieldWithPath("bookItems[].quantity").description("수량"),
                                fieldWithPath("bookItems[].salePrice").description("할인가"),

                                fieldWithPath("deliveryDateResponses").description("배송날짜 리스트"),
                                fieldWithPath("deliveryDateResponses[].value").description("값?"),
                                fieldWithPath("deliveryDateResponses[].displayDate").description("날짜?"),

                                fieldWithPath("packagingOptions").description("포장옵션 리스트"),
                                fieldWithPath("packagingOptions[].packagingOptionId").description("포장옵션 PK"),
                                fieldWithPath("packagingOptions[].name").description("포장옵션 이름"),
                                fieldWithPath("packagingOptions[].price").description("포장옵션 가격")
                        )
                ));
    }

    @Test
    @DisplayName("[주문 전 선택된 아이템 Redis에 저장]")
    void createCheckoutSession() throws Exception {
        // todo: 테스트 데이터 수정 (key값, request 데이터)
        String key = "redis-key";

        OrderCheckoutListRequest request = new OrderCheckoutListRequest(
                List.of(new OrderCheckoutRequest(1L, 2))
        );

        given(orderCheckoutService.initiateCheckout(anyList()))
                .willReturn(key);

        mockMvc.perform(post("/orders/checkout-temp")
                        .param("key", key)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(document("order-checkout-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        // todo: 데이터 설명 수정 필요
                        requestFields(
                                fieldWithPath("items").description("아이템 리스트"),
                                fieldWithPath("items[].bookId").description("도서 PK"),
                                fieldWithPath("items[].quantity").description("수량")
                        )
                ));

    }
}