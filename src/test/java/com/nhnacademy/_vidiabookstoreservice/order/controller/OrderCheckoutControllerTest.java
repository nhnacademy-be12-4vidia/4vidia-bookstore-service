package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutListRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCheckoutRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.DeliveryDateResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderBookResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderCheckoutResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.PackagingOptionResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderCheckoutService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderCheckoutControllerTest extends SupportControllerTest {

    @MockitoBean
    private OrderCheckoutService orderCheckoutService;

    @Test
    @DisplayName("[주문 화면 조회] 주문에 필요한 정보들을 반환한다")
    void getOrderCheckout() throws Exception {
        // Given
        Long userId = 1L;
        String key = "redis-key";

        OrderCheckoutResponse mockResponse = new OrderCheckoutResponse(
                "노인과 바다 외 1권",
                20000,
                List.of(new OrderBookResponse(
                        1L, "노인과 바다", "헤밍웨이", "http://image.url", "800", 2, 10000
                )),
                List.of(new DeliveryDateResponse(LocalDate.now().plusDays(1).toString(), "내일 도착")),
                List.of(new PackagingOptionResponse(1L, "선물 포장", 1000))
        );

        // UserContext Static Mocking
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContextInstance = mock(UserContext.class);
            given(mockContextInstance.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContextInstance);

            given(orderCheckoutService.getOrderCheckoutResponse(eq(userId), eq(key)))
                    .willReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/orders")
                            .param("key", key)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.orderName").value("노인과 바다 외 1권"))
                    .andDo(document("order-checkout-get",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            queryParameters(
                                    parameterWithName("key").description("장바구니/상세페이지에서 생성된 Redis Key")
                            ),
                            responseFields(withHeader(
                                    fieldWithPath("data.orderName").description("주문명"),
                                    fieldWithPath("data.finalAmount").description("최종 가격"),
                                    fieldWithPath("data.bookItems[]").description("도서 목록"),
                                    fieldWithPath("data.bookItems[].bookId").description("도서 ID"),
                                    fieldWithPath("data.bookItems[].bookTitle").description("도서 제목"),
                                    fieldWithPath("data.bookItems[].bookAuthor").description("저자"),
                                    fieldWithPath("data.bookItems[].bookImageUrl").description("이미지 URL"),
                                    fieldWithPath("data.bookItems[].categoryKdc").description("KDC 코드"),
                                    fieldWithPath("data.bookItems[].quantity").description("수량"),
                                    fieldWithPath("data.bookItems[].salePrice").description("판매가"),
                                    fieldWithPath("data.deliveryDateResponses[]").description("배송 가능일 목록"),
                                    fieldWithPath("data.deliveryDateResponses[].value").description("배송일 데이터"),
                                    fieldWithPath("data.deliveryDateResponses[].displayDate").description("배송일 표시 문자열"),
                                    fieldWithPath("data.packagingOptions[]").description("포장 옵션 목록"),
                                    fieldWithPath("data.packagingOptions[].packagingOptionId").description("포장 옵션 ID"),
                                    fieldWithPath("data.packagingOptions[].name").description("포장지 이름"),
                                    fieldWithPath("data.packagingOptions[].price").description("포장 가격")
                            ))
                    ));
        }
    }

    @Test
    @DisplayName("[주문 세션 생성] 선택된 아이템을 임시 저장하고 Key를 반환한다")
    void createCheckoutSession() throws Exception {
        // Given
        String generatedKey = "new-redis-key-123";
        OrderCheckoutListRequest request = new OrderCheckoutListRequest(
                List.of(new OrderCheckoutRequest(101L, 2))
        );

        given(orderCheckoutService.initiateCheckout(anyList()))
                .willReturn(generatedKey);

        // When & Then
        mockMvc.perform(post("/orders/checkout-temp")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // ApiResponse.success()는 기본 200 OK
                .andExpect(jsonPath("$.data").value(generatedKey))
                .andDo(document("order-checkout-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("items[]").description("주문할 도서 목록"),
                                fieldWithPath("items[].bookId").description("도서 ID"),
                                fieldWithPath("items[].quantity").description("수량")
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data").description("생성된 Redis 세션 키")
                        ))
                ));
    }
}