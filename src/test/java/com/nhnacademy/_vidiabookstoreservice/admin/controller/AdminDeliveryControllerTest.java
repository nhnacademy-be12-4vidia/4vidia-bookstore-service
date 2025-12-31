package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminDeliveryService;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminDeliveryController.class)
class AdminDeliveryControllerTest extends SupportControllerTest {

    @MockitoBean
    private AdminDeliveryService adminDeliveryService;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("GET - 배송 목록 조회 (상태별/키워드)")
    void listByDeliveryStatus() throws Exception {
        Order mockOrder = mock(Order.class);
        given(mockOrder.getOrderId()).willReturn(1L);
        given(mockOrder.getDeliveryStatus()).willReturn(DeliveryStatus.WAITING);
        given(mockOrder.getOrderItems()).willReturn(Collections.emptyList());

        Page<Order> orderPage = new PageImpl<>(List.of(mockOrder), PageRequest.of(0, 20), 1);
        given(adminDeliveryService.listByDeliveryStatus(any(), anyString(), any(Pageable.class)))
                .willReturn(orderPage);

        mockMvc.perform(get("/admin/deliveries")
                        .param("deliveryStatus", "WAITING")
                        .param("keyword", "홍길동")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("admin-deliveries-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("deliveryStatus").description("배송 상태").optional(),
                                parameterWithName("keyword").description("검색어").optional(),
                                parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 당 수량").optional()
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.content[].orderId").description("주문 ID"),
                                fieldWithPath("data.content[].email").description("이메일"),
                                fieldWithPath("data.content[].recipientName").description("수령인"),
                                fieldWithPath("data.content[].addressRoadname").description("도로명"),
                                fieldWithPath("data.content[].addressDetail").description("상세주소"),
                                fieldWithPath("data.content[].deliveryStatus").description("상태"),
                                fieldWithPath("data.content[].payPrice").description("결제금액"),
                                fieldWithPath("data.content[].recipientPhone").description("전화번호"),
                                fieldWithPath("data.content[].deliveryRequest").description("요청사항"),
                                fieldWithPath("data.content[].createAt").description("생성일"),
                                fieldWithPath("data.content[].couponDiscount").description("쿠폰할인"),
                                fieldWithPath("data.content[].pointUsed").description("포인트사용"),
                                fieldWithPath("data.content[].deliveryDate").description("희망일"),
                                fieldWithPath("data.content[].actualDeliveryDate").description("완료일"),
                                fieldWithPath("data.content[].orderItems").description("품목"),

                                // Pageable 상세 명세 추가
                                fieldWithPath("data.pageable.pageNumber").description("현재 페이지 번호"),
                                fieldWithPath("data.pageable.pageSize").description("페이지 크기"),
                                fieldWithPath("data.pageable.sort.empty").description("정렬 정보 빈 여부"),
                                fieldWithPath("data.pageable.sort.sorted").description("정렬 여부"),
                                fieldWithPath("data.pageable.sort.unsorted").description("미정렬 여부"),
                                fieldWithPath("data.pageable.offset").description("오프셋"),
                                fieldWithPath("data.pageable.paged").description("페이징 여부"),
                                fieldWithPath("data.pageable.unpaged").description("페이징 미사용 여부"),

                                fieldWithPath("data.last").description("마지막 페이지 여부"),
                                fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.totalElements").description("전체 데이터 수"),
                                fieldWithPath("data.size").description("페이지당 데이터 수"),
                                fieldWithPath("data.number").description("현재 페이지 번호"),
                                fieldWithPath("data.sort.empty").description("정렬 정보 빈 여부"),
                                fieldWithPath("data.sort.sorted").description("정렬 여부"),
                                fieldWithPath("data.sort.unsorted").description("미정렬 여부"),
                                fieldWithPath("data.first").description("첫 페이지 여부"),
                                fieldWithPath("data.numberOfElements").description("현재 페이지 요소 수"),
                                fieldWithPath("data.empty").description("결과 비어있음 여부")
                        ))
                ));
    }

    @Test
    @DisplayName("GET - 배송 상세 조회 (주문 정보)")
    void getOrderDetail() throws Exception {
        Long orderId = 1L;

        Order mockOrder = mock(Order.class);
        given(mockOrder.getOrderId()).willReturn(orderId);
        given(mockOrder.getRecipientName()).willReturn("홍길동");
        given(mockOrder.getRecipientPhone()).willReturn("010-1234-5678");
        given(mockOrder.getAddressRoadname()).willReturn("경기도 성남시 ... 하이패스");
        given(mockOrder.getAddressDetail()).willReturn("NHN 6층");
        given(mockOrder.getDeliveryStatus()).willReturn(DeliveryStatus.SHIPPING);
        given(mockOrder.getTotalBookPrice()).willReturn(50000);
        given(mockOrder.getDeliveryRequest()).willReturn("문 앞에 놔주세요.");
        given(mockOrder.getCreatedAt()).willReturn(LocalDateTime.now());
        given(mockOrder.getCouponDiscount()).willReturn(5000);
        given(mockOrder.getPointUsed()).willReturn(1000);
        given(mockOrder.getDeliveryDate()).willReturn(LocalDate.now().plusDays(1));
        given(mockOrder.getActualDeliveryDate()).willReturn(null);
        given(mockOrder.getOrderItems()).willReturn(Collections.emptyList());

        given(orderService.getOrder(orderId)).willReturn(mockOrder);

        mockMvc.perform(get("/admin/deliveries/{order-id}", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andDo(document("admin-delivery-detail-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("order-id").description("조회할 주문 ID")
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.orderId").description("주문 고유 ID"),
                                fieldWithPath("data.email").description("주문자 이메일 (비회원은 '비회원')"),
                                fieldWithPath("data.recipientName").description("수령인 이름"),
                                fieldWithPath("data.addressRoadname").description("도로명 주소"),
                                fieldWithPath("data.addressDetail").description("상세 주소"),
                                fieldWithPath("data.deliveryStatus").description("배송 상태 (WAITING, SHIPPING, DELIVERED, CANCELED)"),
                                fieldWithPath("data.payPrice").description("총 도서 가격 (결제 금액)"),
                                fieldWithPath("data.recipientPhone").description("수령인 연락처"),
                                fieldWithPath("data.deliveryRequest").description("배송 요청 사항"),
                                fieldWithPath("data.createAt").description("주문 생성 일시"),
                                fieldWithPath("data.couponDiscount").description("쿠폰 할인 금액"),
                                fieldWithPath("data.pointUsed").description("사용한 포인트"),
                                fieldWithPath("data.deliveryDate").description("희망 배송일"),
                                fieldWithPath("data.actualDeliveryDate").description("실제 배송 완료일 (미완료 시 null)").optional(),
                                fieldWithPath("data.orderItems[]").description("주문 도서 목록 (상세)")
                        ))
                ));
    }

    @Test
    @DisplayName("PUT - 배송 시작 처리")
    void startDelivery() throws Exception {
        Long orderId = 1L;
        Order mockOrder = mock(Order.class);
        given(mockOrder.getOrderId()).willReturn(orderId);
        given(mockOrder.getDeliveryStatus()).willReturn(DeliveryStatus.SHIPPING);
        given(mockOrder.getOrderItems()).willReturn(Collections.emptyList());

        given(adminDeliveryService.startDelivery(orderId)).willReturn(mockOrder);

        mockMvc.perform(put("/admin/deliveries/{order-id}/start-delivery", orderId))
                .andExpect(status().isOk())
                .andDo(document("admin-delivery-start-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("order-id").description("주문 ID")),
                        responseFields(withHeader(
                                fieldWithPath("data.orderId").description("주문 ID"),
                                fieldWithPath("data.email").description("이메일"),
                                fieldWithPath("data.recipientName").description("수령인"),
                                fieldWithPath("data.addressRoadname").description("도로명"),
                                fieldWithPath("data.addressDetail").description("상세주소"),
                                fieldWithPath("data.deliveryStatus").description("배송 상태"),
                                fieldWithPath("data.payPrice").description("결제 금액"),
                                fieldWithPath("data.recipientPhone").description("전화번호"),
                                fieldWithPath("data.deliveryRequest").description("요청사항"),
                                fieldWithPath("data.createAt").description("생성일"),
                                fieldWithPath("data.couponDiscount").description("쿠폰 할인액"),
                                fieldWithPath("data.pointUsed").description("사용 포인트"),
                                fieldWithPath("data.deliveryDate").description("희망일"),
                                fieldWithPath("data.actualDeliveryDate").description("완료일"),
                                fieldWithPath("data.orderItems[]").description("주문 상품 목록")
                        ))
                ));
    }

    @Test
    @DisplayName("PUT - 배송 완료 처리")
    void completeDelivery() throws Exception {
        Long orderId = 1L;
        Order mockOrder = mock(Order.class);
        given(mockOrder.getOrderId()).willReturn(orderId);
        given(mockOrder.getDeliveryStatus()).willReturn(DeliveryStatus.DELIVERED);
        given(mockOrder.getOrderItems()).willReturn(Collections.emptyList());

        given(adminDeliveryService.completeDelivery(orderId)).willReturn(mockOrder);

        mockMvc.perform(put("/admin/deliveries/{order-id}/complete-delivery", orderId))
                .andExpect(status().isOk())
                .andDo(document("admin-delivery-complete-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("order-id").description("주문 ID")),
                        responseFields(withHeader(
                                fieldWithPath("data.orderId").description("주문 ID"),
                                fieldWithPath("data.email").description("이메일"),
                                fieldWithPath("data.recipientName").description("수령인"),
                                fieldWithPath("data.addressRoadname").description("도로명"),
                                fieldWithPath("data.addressDetail").description("상세주소"),
                                fieldWithPath("data.deliveryStatus").description("배송 상태"),
                                fieldWithPath("data.payPrice").description("결제 금액"),
                                fieldWithPath("data.recipientPhone").description("전화번호"),
                                fieldWithPath("data.deliveryRequest").description("요청사항"),
                                fieldWithPath("data.createAt").description("생성일"),
                                fieldWithPath("data.couponDiscount").description("쿠폰 할인액"),
                                fieldWithPath("data.pointUsed").description("사용 포인트"),
                                fieldWithPath("data.deliveryDate").description("희망일"),
                                fieldWithPath("data.actualDeliveryDate").description("완료일"),
                                fieldWithPath("data.orderItems[]").description("주문 상품 목록")
                        ))
                ));
    }
}