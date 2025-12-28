package com.nhnacademy._vidiabookstoreservice.cart.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.cart.domain.CartOwner;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.AddCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.request.UpdateCartItemRequest;
import com.nhnacademy._vidiabookstoreservice.cart.dto.response.CartResponse;
import com.nhnacademy._vidiabookstoreservice.cart.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerTest extends SupportControllerTest {

    @MockitoBean
    private CartService cartService;

    private final Long testUserId = 1L;
    private final Long testGuestId = 999L;
    private final Long testBookId = 100L;

    @Test
    @DisplayName("[장바구니 조회 - 회원]")
    void getCart_user() throws Exception {
        // given
        CartResponse response = new CartResponse(testUserId, List.of());
        given(cartService.getCart(any(CartOwner.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/cart")
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserId))
                .andDo(document("cart-get-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("X-User-Id").description("회원 ID")
                        ),
                        responseFields(
                                fieldWithPath("userId").description("회원 ID"),
                                fieldWithPath("items").description("장바구니 아이템 목록")
                        )
                ));
    }

    @Test
    @DisplayName("[장바구니 조회 - 비회원]")
    void getCart_guest() throws Exception {
        // given
        // 비회원이면 userId가 null
        CartResponse response = new CartResponse(null, List.of());
        given(cartService.getCart(any(CartOwner.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/cart")
                        .header("X-Guest-Id", testGuestId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("cart-get-guest",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("X-Guest-Id").description("비회원 식별 ID")
                        ),
                        responseFields(
                                fieldWithPath("userId").description("회원 ID (비회원 null)").optional(),
                                fieldWithPath("items").description("장바구니 아이템 목록")
                        )
                ));
    }

    @Test
    @DisplayName("[비회원 장바구니 상태 확인]")
    void guestCartStatus() throws Exception {
        // given
        given(cartService.countGuestCartItems(testGuestId)).willReturn(5);

        // when & then
        mockMvc.perform(get("/cart/guest/status")
                        .header("X-Guest-Id", testGuestId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasGuestCart").value(true))
                .andExpect(jsonPath("$.itemCount").value(5))
                .andDo(document("cart-guest-status-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("X-Guest-Id").description("비회원 식별 ID")
                        ),
                        responseFields(
                                fieldWithPath("hasGuestCart").description("장바구니 보유 여부"),
                                fieldWithPath("itemCount").description("담긴 아이템 수")
                        )
                ));
    }

    @Test
    @DisplayName("[장바구니 아이템 추가]")
    void addItem() throws Exception {
        // given
        AddCartItemRequest request = new AddCartItemRequest(testBookId, 2);
        willDoNothing().given(cartService).addItem(any(CartOwner.class), any(AddCartItemRequest.class));

        // when & then
        mockMvc.perform(post("/cart/items")
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("cart-item-add-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("X-User-Id").description("회원 ID").optional()
                        ),
                        requestFields(
                                fieldWithPath("bookId").description("도서 ID"),
                                fieldWithPath("quantity").description("수량 (1 이상)")
                        )
                ));
    }

    @Test
    @DisplayName("[장바구니 아이템 수량 수정]")
    void updateCartBook() throws Exception {
        // given
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);
        willDoNothing().given(cartService).updateItem(any(CartOwner.class), eq(testBookId), any(UpdateCartItemRequest.class));

        // when & then
        mockMvc.perform(put("/cart/items/{book-id}", testBookId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("cart-item-update-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("book-id").description("수정할 도서 ID")
                        ),
                        requestFields(
                                fieldWithPath("quantity").description("변경할 수량")
                        )
                ));
    }

    @Test
    @DisplayName("[장바구니 전체 삭제 - 회원]")
    void deleteCart() throws Exception {
        // given
        willDoNothing().given(cartService).deleteCart(testUserId);

        // when & then
        mockMvc.perform(delete("/cart")
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNoContent())
                .andDo(document("cart-delete-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("X-User-Id").description("회원 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[장바구니 특정 아이템 삭제]")
    void deleteItem() throws Exception {
        // given
        willDoNothing().given(cartService).removeItem(any(CartOwner.class), eq(testBookId));

        // when & then
        mockMvc.perform(delete("/cart/items/{book-id}", testBookId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNoContent())
                .andDo(document("cart-item-delete-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("book-id").description("삭제할 도서 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[장바구니 아이템 다중 삭제]")
    void deleteSelectItems() throws Exception {
        // given
        List<Long> itemIds = List.of(1L, 2L, 3L);
        willDoNothing().given(cartService).removeItemByOrder(anyLong(), anyList());

        // when & then
        // [수정] .param() 대신 URL에 직접 쿼리 스트링을 포함시킵니다.
        mockMvc.perform(delete("/cart/items?itemIds=1,2,3")
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNoContent())
                .andDo(document("cart-items-select-delete-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                // URL에 포함된 파라미터를 문서화합니다.
                                parameterWithName("itemIds").description("삭제할 도서 ID 리스트 (콤마로 구분)")
                        )
                ));
    }

    @Test
    @DisplayName("[비회원 장바구니 병합]")
    void mergeGuestToMember() throws Exception {
        // given
        willDoNothing().given(cartService).mergeGuestCartToUser(testGuestId, testUserId);

        // when & then
        mockMvc.perform(post("/cart/merge-guest")
                        .header("X-User-Id", testUserId)
                        .header("X-Guest-Id", testGuestId))
                .andExpect(status().isOk())
                .andDo(document("cart-merge-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("X-User-Id").description("회원 ID"),
                                headerWithName("X-Guest-Id").description("비회원 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[로그아웃 시 동기화]")
    void logoutSync() throws Exception {
        // given
        willDoNothing().given(cartService).logoutSyncCart(testUserId);

        // when & then
        mockMvc.perform(post("/cart/logout-sync")
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNoContent())
                .andDo(document("cart-logout-sync-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("X-User-Id").description("회원 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[로그인 시 동기화]")
    void loginSync() throws Exception {
        // given
        willDoNothing().given(cartService).loginSyncCart(testUserId);

        // when & then
        mockMvc.perform(post("/cart/login-sync")
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNoContent())
                .andDo(document("cart-login-sync-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("X-User-Id").description("회원 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[비회원 장바구니 비우기]")
    void clearGuestCart() throws Exception {
        // given
        willDoNothing().given(cartService).clear(any(CartOwner.class));

        // when & then
        mockMvc.perform(delete("/cart/guest")
                        .header("X-Guest-Id", testGuestId))
                .andExpect(status().isNoContent())
                .andDo(document("cart-guest-clear-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("X-Guest-Id").description("비회원 ID")
                        )
                ));
    }
}