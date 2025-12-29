package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItemViewStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderCountResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderPreviewResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.response.AddressResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.OrderUserResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MyOrderControllerTest extends SupportControllerTest {

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("[내 주문 내역 조회 - 페이징]")
    void getOrderPreview() throws Exception {
        // Given
        Long userId = 1L;
        OrderPreviewResponse.OrderBookResponse bookResponse = new OrderPreviewResponse.OrderBookResponse(
                50L, 1L, "테스트 도서", "테스트 저자", "http://image.url", 1, 15000, OrderItemViewStatus.UNCONFIRMED, false
        );

        OrderPreviewResponse orderPreview = new OrderPreviewResponse(
                100L, userId, LocalDateTime.now(), DeliveryStatus.WAITING, List.of(bookResponse)
        );

        Page<OrderPreviewResponse> page = new PageImpl<>(List.of(orderPreview), PageRequest.of(0, 10), 1);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(orderService.getOrdersByUserId(eq(userId), anyString(), any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/users/me/orders")
                            .header("X-User-Id", userId)
                            .param("page", "0")
                            .param("size", "10")
                            .param("status", "ALL")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].orderId").value(100L))
                    .andDo(document("my-order-preview-get",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(headerWithName("X-User-Id").description("회원 ID")),
                            queryParameters(
                                    parameterWithName("page").description("페이지 번호").optional(),
                                    parameterWithName("size").description("페이지 크기").optional(),
                                    parameterWithName("status").description("주문 상태 필터 (ALL, WAITING, SHIPPING 등)").optional()
                            ),
                            responseFields(withHeader(
                                    fieldWithPath("data.content[].orderId").description("주문 ID"),
                                    fieldWithPath("data.content[].userId").description("회원 ID"),
                                    fieldWithPath("data.content[].createdAt").description("주문 일시"),
                                    fieldWithPath("data.content[].deliveryStatus").description("배송 상태 (WAITING, SHIPPING, DELIVERED, CANCELED)"),
                                    fieldWithPath("data.content[].orderItems[]").description("주문 상품 목록"),
                                    fieldWithPath("data.content[].orderItems[].orderItemId").description("주문 상세 ID"),
                                    fieldWithPath("data.content[].orderItems[].bookId").description("도서 ID"),
                                    fieldWithPath("data.content[].orderItems[].bookTitle").description("도서 제목"),
                                    fieldWithPath("data.content[].orderItems[].bookAuthor").description("저자"),
                                    fieldWithPath("data.content[].orderItems[].bookImageUrl").description("이미지 URL").optional(),
                                    fieldWithPath("data.content[].orderItems[].quantity").description("수량"),
                                    fieldWithPath("data.content[].orderItems[].salePrice").description("판매가"),
                                    fieldWithPath("data.content[].orderItems[].orderItemViewStatus").description("주문 아이템 표시 상태"),
                                    fieldWithPath("data.content[].orderItems[].isReviewed").description("리뷰 작성 여부"),

                                    // PageResponse 필드
                                    fieldWithPath("data.page").description("현재 페이지 번호"),
                                    fieldWithPath("data.size").description("페이지 크기"),
                                    fieldWithPath("data.totalElements").description("전체 요소 수"),
                                    fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                    fieldWithPath("data.last").description("마지막 페이지 여부")
                            ))
                    ));
        }
    }

    @Test
    @DisplayName("[주문 결제 시 유저 정보 조회]")
    void getOrderUserCheckout() throws Exception {
        // Given
        Long userId = 1L;
        AddressResponse addr = new AddressResponse(10L, "우리집", "도로명주소", "12345", "상세주소");
        OrderUserResponse mockResponse = new OrderUserResponse(
                "user@example.com", "홍길동", "01012345678", 5000,
                10L, "우리집", "도로명주소", "12345", "상세주소", List.of(addr)
        );

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(userService.getOrderUser(userId)).willReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/users/me/order-info")
                            .header("X-User-Id", userId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.email").value("user@example.com"))
                    .andDo(document("my-order-info-get",
                            responseFields(withHeader(
                                    fieldWithPath("data.email").description("이메일"),
                                    fieldWithPath("data.name").description("이름"),
                                    fieldWithPath("data.phone").description("전화번호"),
                                    fieldWithPath("data.point").description("보유 포인트"),
                                    fieldWithPath("data.addressId").description("기본 배송지 ID").optional(),
                                    fieldWithPath("data.alias").description("기본 배송지 별칭").optional(),
                                    fieldWithPath("data.roadAddress").description("기본 배송지 도로명").optional(),
                                    fieldWithPath("data.zipCode").description("기본 배송지 우편번호").optional(),
                                    fieldWithPath("data.addressDetail").description("기본 배송지 상세주소").optional(),
                                    fieldWithPath("data.addressResponses[]").description("등록된 전체 주소 목록"),
                                    fieldWithPath("data.addressResponses[].addressId").description("주소 ID"),
                                    fieldWithPath("data.addressResponses[].alias").description("별칭"),
                                    fieldWithPath("data.addressResponses[].roadAddress").description("도로명 주소"),
                                    fieldWithPath("data.addressResponses[].zipCode").description("우편번호"),
                                    fieldWithPath("data.addressResponses[].addressDetail").description("상세 주소")
                            ))
                    ));
        }
    }

    @Test
    @DisplayName("[내 주문 상태별 카운트 조회]")
    void getOrderCounts() throws Exception {
        // Given
        Long userId = 1L;
        OrderCountResponse mockResponse = new OrderCountResponse(10, 2, 3, 4, 1);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(orderService.getOrderCounts(userId)).willReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/users/me/orders/counts")
                            .header("X-User-Id", userId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(10))
                    .andDo(document("my-order-counts-get",
                            responseFields(withHeader(
                                    fieldWithPath("data.total").description("전체 주문 건수"),
                                    fieldWithPath("data.waiting").description("입금/결제 대기 건수"),
                                    fieldWithPath("data.shipping").description("배송 중 건수"),
                                    fieldWithPath("data.delivered").description("배송 완료 건수"),
                                    fieldWithPath("data.canceled").description("취소/환불 건수")
                            ))
                    ));
        }
    }
}