package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderTrackingRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.*;
import com.nhnacademy._vidiabookstoreservice.order.dto.packaging.response.PackagingResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest extends SupportControllerTest {

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("[주문 생성]")
    void createOrder() throws Exception {
        // Given
        Long userId = 1L;
        OrderCreateResponse mockResponse = new OrderCreateResponse(100L);

        OrderCreateRequest.ItemRequestDto item = new OrderCreateRequest.ItemRequestDto(1L, 2, 15000, List.of(10L));
        OrderCreateRequest request = new OrderCreateRequest(
                "홍길동", "대왕판교로", "NHN", "13487", "01012341234",
                "문앞", "1234", LocalDate.now().plusDays(2),
                30000, 1000, 3000, 5000, 1000,
                List.of(item), 5L
        );

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(orderService.saveOrder(eq(userId), any(OrderCreateRequest.class))).willReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/orders")
                            .header("X-User-Id", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.orderId").value(100L))
                    .andDo(document("order-create",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(headerWithName("X-User-Id").description("회원 ID")),
                            requestFields(
                                    fieldWithPath("recipientName").description("수령인 이름"),
                                    fieldWithPath("addressRoadname").description("도로명 주소"),
                                    fieldWithPath("addressDetail").description("상세 주소"),
                                    fieldWithPath("zipCode").description("우편번호"),
                                    fieldWithPath("recipientPhone").description("수령인 연락처"),
                                    fieldWithPath("deliveryRequest").description("배송 요청사항").optional(),
                                    fieldWithPath("orderPassword").description("주문 비밀번호(비회원 용)"),
                                    fieldWithPath("deliveryDate").description("희망 배송일"),
                                    fieldWithPath("totalBookPrice").description("도서 총 가격"),
                                    fieldWithPath("packagingFee").description("총 포장비"),
                                    fieldWithPath("deliveryFee").description("배송비"),
                                    fieldWithPath("couponDiscount").description("쿠폰 할인 금액"),
                                    fieldWithPath("pointUsed").description("사용한 포인트"),
                                    fieldWithPath("orderItems[]").description("주문 도서 목록"),
                                    fieldWithPath("orderItems[].bookId").description("도서 ID"),
                                    fieldWithPath("orderItems[].quantity").description("수량"),
                                    fieldWithPath("orderItems[].salePrice").description("판매가(할인가)"),
                                    fieldWithPath("orderItems[].packagingOptionIds[]").description("포장 옵션 ID 목록"),
                                    fieldWithPath("couponId").description("사용된 쿠폰 ID").optional()
                            ),
                            responseFields(withHeader(
                                    fieldWithPath("data.orderId").description("생성된 주문 ID")
                            ))
                    ));
        }
    }

    @Test
    @DisplayName("[회원 주문 상세 조회]")
    void getOrder() throws Exception {
        // Given
        Long userId = 1L;
        Long orderId = 100L;

        PackagingResponse pack = new PackagingResponse(10L, "선물 포장", 1000);
        OrderResponse.OrderBookResponse book = new OrderResponse.OrderBookResponse(
                50L, 1L, "테스트 도서", "테스트 저자", "http://image.url",
                2, 15000, ConfirmStatus.UNCONFIRMED, List.of(pack), 1000
        );

        OrderResponse mockResponse = new OrderResponse(
                orderId, userId, "홍길동", "도로명", "상세", "12345", "01011112222",
                "요청사항", LocalDateTime.now(), 30000, 1000, 3000, 0, 0,
                LocalDate.now().plusDays(2), null, DeliveryStatus.WAITING, List.of(book)
        );

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(orderService.getOrderResponse(eq(userId), eq(orderId))).willReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/orders/{order-id}", orderId)
                            .header("X-User-Id", userId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.orderId").value(orderId))
                    .andDo(document("order-get-detail",
                            pathParameters(parameterWithName("order-id").description("조회할 주문 ID")),
                            responseFields(withHeader(
                                    fieldWithPath("data.orderId").description("주문 ID"),
                                    fieldWithPath("data.userId").description("회원 ID").optional(),
                                    fieldWithPath("data.recipientName").description("수령인"),
                                    fieldWithPath("data.addressRoadname").description("도로명 주소"),
                                    fieldWithPath("data.addressDetail").description("상세 주소"),
                                    fieldWithPath("data.zipCode").description("우편번호"),
                                    fieldWithPath("data.recipientPhone").description("전화번호"),
                                    fieldWithPath("data.deliveryRequest").description("요청사항").optional(),
                                    fieldWithPath("data.createdAt").description("주문 생성일시"),
                                    fieldWithPath("data.totalBookPrice").description("도서 총액"),
                                    fieldWithPath("data.packagingFee").description("포장 총액"),
                                    fieldWithPath("data.deliveryFee").description("배송비"),
                                    fieldWithPath("data.couponDiscount").description("쿠폰 할인"),
                                    fieldWithPath("data.pointUsed").description("포인트 사용"),
                                    fieldWithPath("data.deliveryDate").description("희망 배송일"),
                                    fieldWithPath("data.actualDeliveryDate").description("실제 배송일").optional(),
                                    fieldWithPath("data.deliveryStatus").description("배송 상태"),
                                    fieldWithPath("data.orderItems[]").description("주문 도서 목록"),
                                    fieldWithPath("data.orderItems[].orderItemId").description("아이템 ID"),
                                    fieldWithPath("data.orderItems[].bookId").description("도서 ID"),
                                    fieldWithPath("data.orderItems[].bookTitle").description("도서 제목"),
                                    fieldWithPath("data.orderItems[].bookAuthor").description("저자"),
                                    fieldWithPath("data.orderItems[].bookImageUrl").description("이미지 URL").optional(),
                                    fieldWithPath("data.orderItems[].quantity").description("수량"),
                                    fieldWithPath("data.orderItems[].salePrice").description("판매가"),
                                    fieldWithPath("data.orderItems[].confirmStatus").description("구매확정 상태"),
                                    fieldWithPath("data.orderItems[].totalPackagingPrice").description("총 포장 비용"),
                                    fieldWithPath("data.orderItems[].packagingResponses[]").description("포장 상세 정보"),
                                    fieldWithPath("data.orderItems[].packagingResponses[].packagingOptionId").description("옵션 ID"),
                                    fieldWithPath("data.orderItems[].packagingResponses[].name").description("옵션 이름"),
                                    fieldWithPath("data.orderItems[].packagingResponses[].price").description("옵션 가격")
                            ))
                    ));
        }
    }

    @Test
    @DisplayName("[주문 결제 금액 조회]")
    void getOrderPayPrice() throws Exception {
        Long orderId = 100L;
        given(orderService.getOrderPayPrice(orderId)).willReturn(new OrderAmountResponse(34000));

        mockMvc.perform(get("/orders/{order-id}/amount", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.payPrice").value(34000))
                .andDo(document("order-get-amount",
                        pathParameters(parameterWithName("order-id").description("주문 ID")),
                        responseFields(withHeader(
                                fieldWithPath("data.payPrice").description("최종 결제 금액")
                        ))
                ));
    }

    @Test
    @DisplayName("[비회원 주문 조회]")
    void getOrder_guest() throws Exception {
        OrderTrackingRequest request = new OrderTrackingRequest(100L, "1234");
        OrderResponse mockResponse = new OrderResponse(
                100L, null, "홍길동", "도로명", "상세", "12345", "01011112222",
                "요청사항", LocalDateTime.now(), 20000, 0, 3000, 0, 0,
                LocalDate.now().plusDays(2), null, DeliveryStatus.WAITING, List.of()
        );

        given(orderService.getGuestOrderResponse(any(OrderTrackingRequest.class))).willReturn(mockResponse);

        mockMvc.perform(post("/orders/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(100L))
                .andDo(document("order-guest-get",
                        requestFields(
                                fieldWithPath("orderId").description("주문 번호"),
                                fieldWithPath("orderPassword").description("주문 비밀번호")
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.orderId").description("주문 ID"),
                                fieldWithPath("data.userId").description("회원 ID(비회원 시 null)").optional(),
                                fieldWithPath("data.recipientName").description("수령인"),
                                fieldWithPath("data.addressRoadname").description("도로명 주소"),
                                fieldWithPath("data.addressDetail").description("상세 주소"),
                                fieldWithPath("data.zipCode").description("우편번호"),
                                fieldWithPath("data.recipientPhone").description("수령인 연락처"),
                                fieldWithPath("data.deliveryRequest").description("배송 요청사항").optional(),
                                fieldWithPath("data.createdAt").description("주문 일시"),
                                fieldWithPath("data.totalBookPrice").description("도서 총액"),
                                fieldWithPath("data.packagingFee").description("포장 총액"),
                                fieldWithPath("data.deliveryFee").description("배송비"),
                                fieldWithPath("data.couponDiscount").description("쿠폰 할인"),
                                fieldWithPath("data.pointUsed").description("포인트 사용"),
                                fieldWithPath("data.deliveryDate").description("배송 예정일"),
                                fieldWithPath("data.actualDeliveryDate").description("실제 배송일").optional(),
                                fieldWithPath("data.deliveryStatus").description("배송 상태"),
                                fieldWithPath("data.orderItems[]").description("주문 상품 목록")
                        ))
                ));
    }

    @Test
    @DisplayName("[주문 취소]")
    void cancelOrder() throws Exception {
        Long orderId = 100L;

        mockMvc.perform(put("/orders/{order-id}/cancel", orderId))
                .andExpect(status().isOk()) // void 반환이므로 200 OK
                .andDo(document("order-cancel",
                        pathParameters(parameterWithName("order-id").description("취소할 주문 ID")),
                        responseFields(withHeader())
                ));
    }

    @Test
    @DisplayName("[구매 확정]")
    void changeConfirmOrder() throws Exception {
        Long orderId = 100L;

        mockMvc.perform(put("/orders/{order-id}/confirm-order", orderId))
                .andExpect(status().isOk())
                .andDo(document("order-confirm",
                        pathParameters(parameterWithName("order-id").description("확정할 주문 ID")),
                        responseFields(withHeader())
                ));
    }
}