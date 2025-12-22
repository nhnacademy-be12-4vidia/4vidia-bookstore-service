package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.user.domain.Address;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.repository.AddressRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MyOrderControllerTest extends SupportControllerTest {

    @Autowired private UserRepository userRepository;
    @Autowired private GradeRepository gradeRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private EntityManager entityManager;

    private Long testUserId;

    @BeforeEach
    void initData() {

        Grade grade = gradeRepository.save(Grade.builder().gradeName(GradeName.WELCOME).pointRate(1).build());

        User user = User.builder()
                .email("order_user@test.com")
                .password("pwd")
                .name("User")
                .phone("01012341234")
                .birthDate(LocalDate.now())
                .grade(grade)
                .build();
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.saveAndFlush(user); // Flush
        this.testUserId = savedUser.getUserId();

        Address address = Address.builder()
                .user(savedUser)
                .alias("집")
                .roadAddress("서울")
                .zipCode("12345")
                .addressDetail("101")
                .build();
        addressRepository.saveAndFlush(address); // Flush

        Book book = bookRepository.saveAndFlush(
                    Book.builder()
                        .title("책")
                        .priceStandard(1000)
                        .priceSales(900)
                        .packagingAvailable(true)
                        .build()
        ); // Flush

        Order order = Order.builder()
                .user(savedUser)
                .totalBookPrice(900)
                .deliveryFee(0)
                .packagingFee(0)
                .couponDiscount(0)
                .pointUsed(0)
                .deliveryDate(LocalDate.now().plusDays(3))
                .recipientName("수령인")
                .recipientPhone("010-0000-0000")
                .zipCode("12345")
                .addressRoadname("주소")
                .addressDetail("상세")
                .build();

        Order savedOrder = orderRepository.saveAndFlush(order); // Flush

        OrderItem orderItem = OrderItem.builder()
                .order(savedOrder)
                .book(book)
                .quantity(1)
                .salePrice(900)
                .confirmStatus(ConfirmStatus.UNCONFIRMED)
                .build();
        orderItemRepository.saveAndFlush(orderItem); // Flush

        // 1차 캐시를 비워서 조회 시 SELECT 쿼리가 나가게 함 (필수)
        entityManager.clear();
    }

    @Test
    @DisplayName("[주문내역 미리보기]")
    void getOrderPreview() throws Exception {
        mockMvc.perform(get("/users/me/orders")
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("user-me-orders-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].orderId").description("주문 아이디"),
                                fieldWithPath("[].userId").description("유저 아이디"),
                                fieldWithPath("[].createdAt").description("주문 생성일"),
                                fieldWithPath("[].deliveryStatus").description("배송상태"),
                                fieldWithPath("[].orderItems").description("주문 도서 목록"),
                                fieldWithPath("[].orderItems[].orderItemId").description("주문 상품 아이디"),
                                fieldWithPath("[].orderItems[].bookId").description("도서 아이디"),
                                fieldWithPath("[].orderItems[].bookTitle").description("도서 제목"),
                                fieldWithPath("[].orderItems[].bookAuthor").description("도서 저자").optional(),
                                fieldWithPath("[].orderItems[].bookImageUrl").description("도서 이미지").optional(),
                                fieldWithPath("[].orderItems[].quantity").description("주문 수량"),
                                fieldWithPath("[].orderItems[].salePrice").description("구매 당시 가격"),
                                fieldWithPath("[].orderItems[].confirmStatus").description("주문 확정 상태"),
                                fieldWithPath("[].orderItems[].isReviewed").description("리뷰 작성 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[주문화면에 필요한 유저 정보 조회]")
    void getOrderInfo() throws Exception {
        mockMvc.perform(get("/users/me/order-info")
                        .header("X-User-Id", testUserId)
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
                                fieldWithPath("addressId").description("기본주소 PK").optional(),
                                fieldWithPath("alias").description("별칭").optional(),
                                fieldWithPath("roadAddress").description("도로명 주소").optional(),
                                fieldWithPath("zipCode").description("우편번호").optional(),
                                fieldWithPath("addressDetail").description("상세주소").optional(),
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