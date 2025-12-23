package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentConfirmRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentFailRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.TossPaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import com.nhnacademy._vidiabookstoreservice.order.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest extends SupportControllerTest {

    @MockitoBean private PaymentService<TossPaymentResponse> paymentService;
    @MockitoBean private OrderService orderService;

    @BeforeEach
    void initData() { }

    @Test
    @DisplayName("[결제 조회]")
    void getPayment() throws Exception {
        Long orderId = 100L;

        PaymentResponse response = new PaymentResponse(
                orderId,
                "DONE",
                15000
        );

        given(paymentService.getPayment(orderId)).willReturn(response);

        mockMvc.perform(get("/payments/{orderId}", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("order-payment-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        pathParameters(
                                parameterWithName("orderId").description("주문 ID")
                        ),
                        responseFields(
                                fieldWithPath("orderId").description("주문 ID"),
                                fieldWithPath("payStatus").description("결제 상태"),
                                fieldWithPath("amount").description("결제 금액")
                        )
                ));

    }

    @Test
    @DisplayName("[결제 확정&저장]")
    void savePaymentDetail() throws Exception {
        Long orderId = 100L;

        PaymentConfirmRequest request = new PaymentConfirmRequest(
                "payment_key",
                orderId.toString(),
                15000
        );

        PaymentResponse response = new PaymentResponse(
                orderId,
                "DONE",
                15000
        );

        given(orderService.payAndCompleteOrder(eq(orderId), any(PaymentConfirmRequest.class), any()))
                .willReturn(response);

        mockMvc.perform(post("/payments")
                        .param("id", orderId.toString())
                        .header("X-User-Id", 1L) // 회원 ID 헤더 모의
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("order-payment-confirm-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID").optional()
                        ),
                        queryParameters(
                                parameterWithName("id").description("주문 ID (PK)")
                        ),
                        requestFields(
                                fieldWithPath("paymentKey").description("결제 키"),
                                fieldWithPath("orderId").description("주문 번호 (Toss 요청용 ID)"),
                                fieldWithPath("amount").description("결제 금액")
                        ),
                        responseFields(
                                fieldWithPath("orderId").description("주문 ID"),
                                fieldWithPath("payStatus").description("결제 키"),
                                fieldWithPath("amount").description("결제 금액")
                        )
                ));
    }

    @Test
    @DisplayName("[프론트 결제 처리 중 실패]")
    void rollbackPayment() throws Exception {
        PaymentFailRequest request = new PaymentFailRequest(100L);

        mockMvc.perform(post("/payments/rollback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(document("order-payment-fail-rollback-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("orderId").description("실패한 주문 ID")
                        )
                ));

        verify(orderService).cancelOrder(eq(100L), anyString());
    }
}