package com.nhnacademy._vidiabookstoreservice.cart.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.AddCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.UpdateCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.BookSummaryResponse;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.CartBookResponse;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.CartResponse;
import com.nhnacademy._vidiabookstoreservice.cart.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest extends SupportControllerTest {

    @MockitoBean
    private CartService cartService;

    @Test
    @Order(1)
    @DisplayName("GET - 장바구니 조회")
    void getCart() throws Exception {
        BookSummaryResponse book = new BookSummaryResponse(1L, "테스트 도서", 10000, 9000, "image.jpg");
        CartBookResponse item = new CartBookResponse(book, 2);
        CartResponse response = new CartResponse(1L, List.of(item));

        given(cartService.getCart(any())).willReturn(response);

        mockMvc.perform(get("/cart")
                        .header("X-User-Id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("cart-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                        responseFields(withHeader(
                                fieldWithPath("data.userId").description("사용자 ID"),
                                fieldWithPath("data.items[]").description("장바구니 아이템 목록"),
                                fieldWithPath("data.items[].book.id").description("도서 ID"),
                                fieldWithPath("data.items[].book.title").description("도서 제목"),
                                fieldWithPath("data.items[].book.priceStandard").description("정가"),
                                fieldWithPath("data.items[].book.priceSales").description("판매가"),
                                fieldWithPath("data.items[].book.imageUrl").description("이미지 URL"),
                                fieldWithPath("data.items[].quantity").description("수량")
                        ))
                ));
    }

    @Test
    @Order(2)
    @DisplayName("POST - 장바구니 아이템 추가")
    void addItem() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest(1L, 3);

        mockMvc.perform(post("/cart/items")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("cart-item-add-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                        requestFields(
                                fieldWithPath("bookId").description("도서 ID"),
                                fieldWithPath("quantity").description("추가 수량")
                        ),
                        responseFields(withHeader())
                ));
    }

    @Test
    @Order(3)
    @DisplayName("PUT - 장바구니 아이템 수량 수정")
    void updateCartBook() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        mockMvc.perform(put("/cart/items/{book-id}", 1L)
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("cart-item-update-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                        pathParameters(parameterWithName("book-id").description("도서 ID")),
                        requestFields(fieldWithPath("quantity").description("변경할 수량")),
                        responseFields(withHeader())
                ));
    }

    @Test
    @Order(4)
    @DisplayName("DELETE - 장바구니 전체 삭제 (회원)")
    void deleteCart() throws Exception {
        mockMvc.perform(delete("/cart")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andDo(document("cart-all-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                        responseFields(withHeader())
                ));
    }

    @Test
    @Order(5)
    @DisplayName("DELETE - 장바구니 아이템 삭제 (단일)")
    void deleteItem() throws Exception {
        mockMvc.perform(delete("/cart/items/{book-id}", 1L)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andDo(document("cart-item-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                        pathParameters(parameterWithName("book-id").description("삭제할 도서 ID")),
                        responseFields(withHeader())
                ));
    }

    @Test
    @Order(6)
    @DisplayName("DELETE - 장바구니 아이템 삭제 (다중)")
    void deleteSelectItems() throws Exception {
        mockMvc.perform(delete("/cart/items?itemIds=1&itemIds=2&itemIds=3")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("cart-items-selected-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                        queryParameters(
                                parameterWithName("itemIds").description("삭제할 도서 ID 리스트 (예: itemIds=1&itemIds=2)")
                        ),
                        responseFields(withHeader())
                ));
    }

    @Test
    @Order(7)
    @DisplayName("GET - 장바구니 상태 확인 (비회원)")
    void guestCartStatus() throws Exception {
        given(cartService.countGuestCartItems(anyLong())).willReturn(3);

        mockMvc.perform(get("/cart/guest/status")
                        .header("X-Guest-Id", "12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasGuestCart").value(true))
                .andExpect(jsonPath("$.data.itemCount").value(3))
                .andDo(document("cart-guest-status-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(headerWithName("X-Guest-Id").description("비회원 고유 ID")),
                        responseFields(withHeader(
                                fieldWithPath("data.hasGuestCart").description("비회원 장바구니 존재 여부"),
                                fieldWithPath("data.itemCount").description("장바구니 아이템 개수")
                        ))
                ));
    }

    @Test
    @Order(8)
    @DisplayName("POST - 장바구니 병합 (비회원 -> 회원)")
    void mergeGuestToMember() throws Exception {
        mockMvc.perform(post("/cart/merge-guest")
                        .header("X-User-Id", "1")
                        .header("X-Guest-Id", "12345"))
                .andExpect(status().isOk())
                .andDo(document("cart-merge- post",
                        requestHeaders(
                                headerWithName("X-User-Id").description("회원 고유 ID"),
                                headerWithName("X-Guest-Id").description("비회원 고유 ID")
                        ),
                        responseFields(withHeader())
                ));
    }

    @Test
    @Order(9)
    @DisplayName("DELETE - 장바구니 삭제 (비회원)")
    void clearGuestCart() throws Exception {
        mockMvc.perform(delete("/cart/guest")
                        .header("X-Guest-Id", "12345"))
                .andExpect(status().isOk())
                .andDo(document("cart-guest-clear-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(headerWithName("X-Guest-Id").description("비회원 ID")),
                        responseFields(withHeader())
                ));
    }

    @Test
    @Order(10)
    @DisplayName("POST - 로그인 시 동기화 (DB -> Redis)")
    void loginSync() throws Exception {
        mockMvc.perform(post("/cart/login-sync")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andDo(document("cart-login-sync-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                        responseFields(withHeader())
                ));
    }

    @Test
    @Order(11)
    @DisplayName("POST - 로그아웃 시 동기화 (Redis -> DB)")
    void logoutSync() throws Exception {
        mockMvc.perform(post("/cart/logout-sync")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andDo(document("cart-logout-sync-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                        responseFields(withHeader())
                ));
    }
}