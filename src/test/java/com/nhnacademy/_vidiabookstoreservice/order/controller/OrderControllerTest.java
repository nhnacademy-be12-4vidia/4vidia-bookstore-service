package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.domain.Publisher;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.StockStatus;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.CategoryRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.PublisherRepository;
import com.nhnacademy._vidiabookstoreservice.order.domain.*;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.OrderStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderTrackingRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.TossPaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.repository.*;
import com.nhnacademy._vidiabookstoreservice.order.service.PaymentService;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest extends SupportControllerTest {

    @Autowired private UserRepository userRepository;
    @Autowired private GradeRepository gradeRepository;
    @Autowired private PublisherRepository publisherRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private PackagingOptionRepository packagingOptionRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private PackagingRepository packagingRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private EntityManager entityManager;

    @MockitoBean private PaymentService paymentService;

    private Long testUserId;
    private Long testBookId;
    private Long testOrderId;
    private Long testPackagingOptionId;

    @BeforeEach
    void initData() {
        Grade grade = gradeRepository.save(Grade.builder().gradeName(GradeName.WELCOME).pointRate(1).build());

        User user = User.builder()
                .email("gildong@test.com")
                .password("1234qwer!")
                .name("홍길동")
                .phone("01012341234")
                .birthDate(LocalDate.now())
                .grade(grade)
                .build();
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.saveAndFlush(user);
        this.testUserId = savedUser.getUserId();

        Publisher publisher = publisherRepository.save(Publisher.builder()
                .name("NHN Publishing")
                .build());

        Category category = categoryRepository.save(Category.builder()
                .categoryName("국내도서")
                .kdcCode("001")
                .path("/1")
                .depth(1)
                .build());

        PackagingOption option = packagingOptionRepository.save(PackagingOption.builder()
                .name("선물용 포장")
                .price(1000)
                .build());
        this.testPackagingOptionId = option.getPackagingOptionId();

         Book book = bookRepository.save(Book.builder()
                .isbn("1234567890123")
                .title("테스트 도서 1")
                .priceStandard(20000)
                .priceSales(15000)
                .stock(100)
                .publisher(publisher)
                .category(category)
                .packagingAvailable(false)
                .stockStatus(StockStatus.IN_STOCK)
                .build());
         this.testBookId = book.getId();

        Order order = Order.builder()
                .user(user)
                .recipientName("홍길동")
                .addressRoadname("성남시 분당구...")
                .addressDetail("NHN")
                .zipCode("12345")
                .recipientPhone("010-1111-2222")
                .deliveryRequest("문 앞")
                .orderPassword("1234")
                .totalBookPrice(15000)
                .deliveryFee(3000)
                .packagingFee(1000)
                .couponDiscount(0)
                .pointUsed(0)
                .deliveryDate(LocalDate.now().plusDays(3))
                .build();
        order.setOrderStatus(OrderStatus.PAID);

        Order savedOrder = orderRepository.save(order);
        this.testOrderId = savedOrder.getOrderId();

        Book savedBook = bookRepository.findById(testBookId).orElseThrow();

        OrderItem orderItem = OrderItem.builder()
                .order(savedOrder)
                .book(savedBook)
                .quantity(1)
                .salePrice(15000)
                .confirmStatus(ConfirmStatus.UNCONFIRMED)
                .build();
        OrderItem savedItem = orderItemRepository.save(orderItem);

        PackagingOption savedOption = packagingOptionRepository.findById(testPackagingOptionId).orElseThrow();

        Packaging packaging = Packaging.builder()
                .orderItem(savedItem)
                .packagingOption(savedOption)
                .build();

        packagingRepository.save(packaging);

        Payment payment = Payment.builder()
                .order(savedOrder)
                .payStatus("DONE")
                .payMethod("CARD")
                .amount(19000) // 총 금액
                .paymentKey("test_payment_key")
                .sendOrderId("test_toss_order_id")
                .build();
        paymentRepository.save(payment);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("[주문 생성&저장]")
    void createOrder() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest(
                "홍길동",
                "경기도 성남시 분당구 대왕판교로645번길 16",
                "NHN 본사",
                "13487",
                "01012341234",
                "경비실에 보관해주세요.",
                "1234",
                LocalDate.now(),
                15000,
                1000,
                3000,
                0,
                0,
                List.of(new OrderCreateRequest.ItemRequestDto(testBookId, 1, 15000, List.of(testPackagingOptionId))),
                null
        );

        mockMvc.perform(post("/orders")
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andDo(document("order-create-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        requestFields(
                                fieldWithPath("recipientName").description("수령인 이름"),
                                fieldWithPath("addressRoadname").description("도로명 주소"),
                                fieldWithPath("addressDetail").description("상세주소"),
                                fieldWithPath("zipCode").description("우편번호"),
                                fieldWithPath("recipientPhone").description("수령인 전화번호"),
                                fieldWithPath("deliveryRequest").description("배송 요청사항").optional(),
                                fieldWithPath("orderPassword").description("주문 비밀번호(비회원)"),
                                fieldWithPath("deliveryDate").description("배송날짜").optional(),
                                fieldWithPath("totalBookPrice").description("총 도서 가격"),
                                fieldWithPath("packagingFee").description("포장 수수료"),
                                fieldWithPath("deliveryFee").description("배송 수수료"),
                                fieldWithPath("couponDiscount").description("쿠폰 할인액"),
                                fieldWithPath("pointUsed").description("사용 포인트"),
                                fieldWithPath("orderItems").description("주문 아이템(도서)"),

                                fieldWithPath("orderItems[].bookId").description("도서 PK"),
                                fieldWithPath("orderItems[].quantity").description("도서 수량"),
                                fieldWithPath("orderItems[].salePrice").description("할인가"),
                                fieldWithPath("orderItems[].packagingOptionIds").description("포장 PKs"),

                                fieldWithPath("couponId").description("쿠폰 PK").optional()
                        ),
                        responseFields(
                                fieldWithPath("orderId").description("주문 PK").optional()
                        )
                ));
    }

    @Test
    @DisplayName("[주문 결제 금액 조회]")
    void getOrderPayPrice() throws Exception {
        // 예상 결제 금액 계산: 도서가격(15000) + 포장비(1000) + 배송비(3000) - 쿠폰(0) - 포인트(0) = 19000
        int expectedAmount = 19000;

        mockMvc.perform(get("/orders/{order-id}/amount", testOrderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payPrice").value(expectedAmount)) // 금액 검증
                .andDo(document("order-amount-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("order-id").description("조회할 주문 ID")
                        ),
                        responseFields(
                                fieldWithPath("payPrice").description("최종 결제 금액")
                        )
                ));
    }

    @Test
    @DisplayName("[주문내역 상세보기]")
    void getOrder_user() throws Exception {

        mockMvc.perform(get("/orders/{orderId}", testOrderId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andDo(document("order-find-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        pathParameters(
                                parameterWithName("orderId").description("조회할 주문 ID")
                        ),
                        responseFields(
                                fieldWithPath("orderId").description("주문 ID (PK)"),
                                fieldWithPath("userId").description("회원 ID (비회원인 경우 null)").optional(),
                                fieldWithPath("recipientName").description("수령인 이름"),
                                fieldWithPath("addressRoadname").description("도로명 주소"),
                                fieldWithPath("addressDetail").description("상세 주소"),
                                fieldWithPath("zipCode").description("우편번호"),
                                fieldWithPath("recipientPhone").description("수령인 전화번호"),
                                fieldWithPath("deliveryRequest").description("배송 요청사항 (없으면 null)").optional(),
                                fieldWithPath("createdAt").description("주문 생성 일시"),

                                fieldWithPath("totalBookPrice").description("총 도서 가격 (포장비 제외)"),
                                fieldWithPath("packagingFee").description("총 포장 비용"),
                                fieldWithPath("deliveryFee").description("배송비"),
                                fieldWithPath("couponDiscount").description("쿠폰 할인 금액"),
                                fieldWithPath("pointUsed").description("사용 포인트"),

                                fieldWithPath("deliveryDate").description("희망 배송일").optional(),
                                fieldWithPath("actualDeliveryDate").description("실제 배송 완료일 (배송 전이면 null)").optional(),
                                fieldWithPath("deliveryStatus").description("배송 상태 (PENDING, SHIPPING, DONE 등)"),

                                fieldWithPath("orderItems").description("주문 도서 목록"),
                                fieldWithPath("orderItems[].orderItemId").description("주문 상세 ID (PK)"),
                                fieldWithPath("orderItems[].bookId").description("도서 ID"),
                                fieldWithPath("orderItems[].bookTitle").description("도서 제목"),
                                fieldWithPath("orderItems[].bookAuthor").description("도서 저자 (없으면 '저자 미상')"),
                                fieldWithPath("orderItems[].bookImageUrl").description("도서 이미지 URL (없으면 null)").optional(),
                                fieldWithPath("orderItems[].quantity").description("주문 수량"),
                                fieldWithPath("orderItems[].salePrice").description("도서 판매가 (1권당)"),
                                fieldWithPath("orderItems[].confirmStatus").description("구매 확정 상태 (UNCONFIRMED, CONFIRMED)"),
                                fieldWithPath("orderItems[].totalPackagingPrice").description("해당 도서에 적용된 포장비 합계"),

                                fieldWithPath("orderItems[].packagingResponses").description("적용된 포장 옵션 목록 (없으면 빈 배열)"),
                                fieldWithPath("orderItems[].packagingResponses[].packagingOptionId").description("포장 옵션 ID"),
                                fieldWithPath("orderItems[].packagingResponses[].name").description("포장 옵션 명"),
                                fieldWithPath("orderItems[].packagingResponses[].price").description("포장 가격")
                        )
                ));
    }

    @Test
    @DisplayName("[비회원 주문내역 상세보기]")
    void getOrder_guest() throws Exception {
        Order order = orderRepository.findById(testOrderId).orElseThrow();
        order.setUser(null);
        orderRepository.saveAndFlush(order);

        OrderTrackingRequest request = new OrderTrackingRequest(
                testOrderId,
                "1234"
        );

        mockMvc.perform(post("/orders/guest")
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("order-find-guest-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("orderId").description("주문 번호"),
                                fieldWithPath("orderPassword").description("주문조회 비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("orderId").description("주문 ID (PK)"),
                                fieldWithPath("userId").description("회원 ID (비회원인 경우 null)").optional(),
                                fieldWithPath("recipientName").description("수령인 이름"),
                                fieldWithPath("addressRoadname").description("도로명 주소"),
                                fieldWithPath("addressDetail").description("상세 주소"),
                                fieldWithPath("zipCode").description("우편번호"),
                                fieldWithPath("recipientPhone").description("수령인 전화번호"),
                                fieldWithPath("deliveryRequest").description("배송 요청사항 (없으면 null)").optional(),
                                fieldWithPath("createdAt").description("주문 생성 일시"),

                                fieldWithPath("totalBookPrice").description("총 도서 가격 (포장비 제외)"),
                                fieldWithPath("packagingFee").description("총 포장 비용"),
                                fieldWithPath("deliveryFee").description("배송비"),
                                fieldWithPath("couponDiscount").description("쿠폰 할인 금액"),
                                fieldWithPath("pointUsed").description("사용 포인트"),

                                fieldWithPath("deliveryDate").description("희망 배송일").optional(),
                                fieldWithPath("actualDeliveryDate").description("실제 배송 완료일 (배송 전이면 null)").optional(),
                                fieldWithPath("deliveryStatus").description("배송 상태 (PENDING, SHIPPING, DONE 등)"),

                                fieldWithPath("orderItems").description("주문 도서 목록"),
                                fieldWithPath("orderItems[].orderItemId").description("주문 상세 ID (PK)"),
                                fieldWithPath("orderItems[].bookId").description("도서 ID"),
                                fieldWithPath("orderItems[].bookTitle").description("도서 제목"),
                                fieldWithPath("orderItems[].bookAuthor").description("도서 저자 (없으면 '저자 미상')"),
                                fieldWithPath("orderItems[].bookImageUrl").description("도서 이미지 URL (없으면 null)").optional(),
                                fieldWithPath("orderItems[].quantity").description("주문 수량"),
                                fieldWithPath("orderItems[].salePrice").description("도서 판매가 (1권당)"),
                                fieldWithPath("orderItems[].confirmStatus").description("구매 확정 상태 (UNCONFIRMED, CONFIRMED)"),
                                fieldWithPath("orderItems[].totalPackagingPrice").description("해당 도서에 적용된 포장비 합계"),

                                fieldWithPath("orderItems[].packagingResponses").description("적용된 포장 옵션 목록 (없으면 빈 배열)"),
                                fieldWithPath("orderItems[].packagingResponses[].packagingOptionId").description("포장 옵션 ID"),
                                fieldWithPath("orderItems[].packagingResponses[].name").description("포장 옵션 명"),
                                fieldWithPath("orderItems[].packagingResponses[].price").description("포장 가격")
                        )
                ));
    }

    @Test
    @DisplayName("[배송 전 추문취소]")
    void cancelOrder() throws Exception {
        // 1. Mock: getPaymentEntity 호출 시 DB에 저장해둔 Payment 반환
        Payment savedPayment = paymentRepository.findPaymentByOrder_orderId(testOrderId).orElseThrow();
        given(paymentService.getPaymentEntity(anyLong())).willReturn(savedPayment);

        // 2. Mock: cancelPayment 호출 시 Toss 응답 객체(Dummy) 반환
        TossPaymentResponse.Cancel mockCancel = new TossPaymentResponse.Cancel(
                19000L, "단순 변심", 0L, 0, 19000L, 0L, 0L, 0L,
                "2024-01-01T12:00:00", "key", "rKey", "DONE", "reqId"
        );

        TossPaymentResponse mockResponse = new TossPaymentResponse(
                "1.0", "test_key", "NORMAL", "orderId", "orderName", "mid", "KRW", "CARD",
                19000, 0, "CANCELED", "2024-01-01", "2024-01-01", false, "tKey", 19000, "0", false, 0, 0,
                List.of(mockCancel), // 취소 내역 포함
                true, null, null, null, null, null, null, null, null, null, null, "KR", null, null, null, null
        );

        given(paymentService.cancelPayment(any(), any(), anyLong())).willReturn(mockResponse);

        // 3. Mock: savePayment 호출 시 아무것도 하지 않음 (또는 null 반환)
        given(paymentService.savePayment(any())).willReturn(null);

        mockMvc.perform(put("/orders/{orderId}/cancel", testOrderId))
                .andExpect(status().isNoContent())
                .andDo(document("order-cancel-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        pathParameters(
                                parameterWithName("orderId").description("취소할 주문 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[주문 구매확정]")
    void changeConfirmOrder() throws Exception {
        mockMvc.perform(put("/orders/{order-id}/confirm-order", testOrderId))
                .andExpect(status().isNoContent())
                .andDo(document("order-confirm-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        pathParameters(
                                parameterWithName("order-id").description("구매확정할 주문 ID")
                        )
                ));
    }
}