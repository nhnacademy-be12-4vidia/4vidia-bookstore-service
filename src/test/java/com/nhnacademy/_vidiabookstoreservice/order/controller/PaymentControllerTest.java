package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentConfirmRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentFailRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import com.nhnacademy._vidiabookstoreservice.order.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest extends SupportControllerTest {

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private OrderService orderService;

    @Test
    @Order(1)
    @DisplayName("GET - 결제 내역 조회")
    void getPayment() throws Exception {
        long orderId = 100L;
        PaymentResponse response = new PaymentResponse(orderId, "DONE", 25000);

        given(paymentService.getPayment(orderId)).willReturn(response);

        mockMvc.perform(get("/payments/{order-id}", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.payStatus").value("DONE"))
                .andDo(document("payment-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("order-id").description("주문 ID")
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.orderId").description("주문 ID"),
                                fieldWithPath("data.payStatus").description("결제 상태"),
                                fieldWithPath("data.amount").description("결제 금액")
                        ))
                ));
    }

    @Test
    @Order(2)
    @DisplayName("POST - 결제 확정 및 저장 (회원)")
    void savePaymentDetail_Member() throws Exception {
        long orderId = 100L;
        Long userId = 1L;
        PaymentConfirmRequest request = new PaymentConfirmRequest("payment-key-123", "toss-order-id", 25000);
        PaymentResponse response = new PaymentResponse(orderId, "DONE", 25000);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            given(mockContext.getGuestId()).willReturn(null);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(orderService.payAndCompleteOrder(eq(orderId), any(PaymentConfirmRequest.class), eq(userId)))
                    .willReturn(response);

            mockMvc.perform(post("/payments")
                            .param("id", String.valueOf(orderId))
                            .header("X-User-Id", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(document("payment-confirm-member-post",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName("X-User-Id").description("회원 고유 ID")
                            ),
                            queryParameters(
                                    parameterWithName("id").description("시스템 주문 ID")
                            ),
                            requestFields(
                                    fieldWithPath("paymentKey").description("토스 페이먼츠 결제 키"),
                                    fieldWithPath("orderId").description("토스 페이먼츠 주문 번호"),
                                    fieldWithPath("amount").description("결제 금액")
                            ),
                            responseFields(withHeader(
                                    fieldWithPath("data.orderId").description("주문 ID"),
                                    fieldWithPath("data.payStatus").description("결제 상태"),
                                    fieldWithPath("data.amount").description("결제 금액")
                            ))
                    ));
        }
    }
    @Test
    @Order(3)
    @DisplayName("POST - 결제 확정 및 저장 (비회원)")
    void savePaymentDetail_Guest() throws Exception {
        long orderId = 101L;
        Long guestId = 999L; // 임의의 비회원 ID
        PaymentConfirmRequest request = new PaymentConfirmRequest("guest-payment-key", "guest-order-id", 15000);
        PaymentResponse response = new PaymentResponse(orderId, "DONE", 15000);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(null); // 회원이 아님
            given(mockContext.getGuestId()).willReturn(guestId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(orderService.payAndCompleteOrder(eq(orderId), any(PaymentConfirmRequest.class), eq(guestId)))
                    .willReturn(response);

            mockMvc.perform(post("/payments")
                            .param("id", String.valueOf(orderId))
                            .header("X-Guest-Id", guestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.orderId").value(orderId))
                    .andDo(document("payment-confirm-guest-post",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName("X-Guest-Id").description("비회원 고유 ID")
                            ),
                            queryParameters(
                                    parameterWithName("id").description("시스템 주문 ID")
                            ),
                            requestFields(
                                    fieldWithPath("paymentKey").description("토스 페이먼츠 결제 키"),
                                    fieldWithPath("orderId").description("토스 페이먼츠 주문 번호"),
                                    fieldWithPath("amount").description("결제 금액")
                            ),
                            responseFields(withHeader(
                                    fieldWithPath("data.orderId").description("주문 ID"),
                                    fieldWithPath("data.payStatus").description("결제 상태"),
                                    fieldWithPath("data.amount").description("결제 금액")
                            ))
                    ));
        }
    }

    @Test
    @Order(4)
    @DisplayName("POST - 결제 중 실패(롤백)")
    void rollbackPayment() throws Exception {
        PaymentFailRequest request = new PaymentFailRequest(100L);

        mockMvc.perform(post("/payments/rollback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("payment-rollback-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("orderId").description("실패한 주문 ID")
                        ),
                        responseFields(withHeader())
                ));
    }
}