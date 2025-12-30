package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.request.AddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.request.CreateAddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.response.AddressResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.AddressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

@WebMvcTest(AddressController.class)
class AddressControllerTest extends SupportControllerTest {

    @MockitoBean
    private AddressService addressService;


    @Test
    @Order(1)
    @DisplayName("GET - 주소 단일 조회")
    void getAddress() throws Exception {
        Long userId = 1L;
        Long addressId = 10L;
        AddressResponse response = new AddressResponse(addressId, "우리집", "도로명주소", "12345", "상세주소");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(addressService.getAddress(userId, addressId)).willReturn(response);

            mockMvc.perform(get("/users/me/addresses/{address-id}", addressId)
                            .header("X-User-Id", userId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.addressId").value(addressId))
                    .andDo(document("address-detail-get",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                            pathParameters(parameterWithName("address-id").description("조회할 주소 ID")),
                            responseFields(withHeader(
                                    fieldWithPath("data.addressId").description("주소 ID"),
                                    fieldWithPath("data.alias").description("별칭"),
                                    fieldWithPath("data.roadAddress").description("도로명 주소"),
                                    fieldWithPath("data.zipCode").description("우편번호"),
                                    fieldWithPath("data.addressDetail").description("상세 주소")
                            ))
                    ));
        }
    }

    @Test
    @Order(2)
    @DisplayName("GET - 주소 전체 조회")
    void getAddressList() throws Exception {
        Long userId = 1L;
        AddressResponse addr = new AddressResponse(10L, "집", "도로명", "12345", "상세");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(addressService.getUserAddresses(userId)).willReturn(List.of(addr));

            mockMvc.perform(get("/users/me/addresses")
                            .header("X-User-Id", userId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].alias").value("집"))
                    .andDo(document("address-list-get",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                            responseFields(withHeader(
                                    fieldWithPath("data[].addressId").description("주소 ID"),
                                    fieldWithPath("data[].alias").description("별칭"),
                                    fieldWithPath("data[].roadAddress").description("도로명 주소"),
                                    fieldWithPath("data[].zipCode").description("우편번호"),
                                    fieldWithPath("data[].addressDetail").description("상세 주소")
                            ))
                    ));
        }
    }

    @Test
    @Order(3)
    @DisplayName("POST - 주소 등록")
    void registerAddress() throws Exception {
        Long userId = 1L;
        CreateAddressRequest request = new CreateAddressRequest("우리집", "경기도 성남시 ...", "12345", "NHN 6층");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            mockMvc.perform(post("/users/me/addresses")
                            .header("X-User-Id", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andDo(document("address-register-post",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                            requestFields(
                                    fieldWithPath("alias").description("주소 별칭"),
                                    fieldWithPath("roadAddress").description("도로명 주소"),
                                    fieldWithPath("zipCode").description("우편번호"),
                                    fieldWithPath("addressDetail").description("상세 주소").optional()
                            ),
                            responseFields(withHeader())
                    ));
        }
    }

    @Test
    @Order(4)
    @DisplayName("PUT - 주소 수정")
    void updateAddress() throws Exception {
        Long userId = 1L;
        Long addressId = 10L;
        AddressRequest request = new AddressRequest("수정된 별칭", "수정된 도로명", "54321", "수정된 상세");
        AddressResponse response = new AddressResponse(addressId, "수정된 별칭", "수정된 도로명", "54321", "수정된 상세");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(addressService.updateAddress(eq(userId), eq(addressId), any(AddressRequest.class))).willReturn(response);

            mockMvc.perform(put("/users/me/addresses/{address-id}", addressId)
                            .header("X-User-Id", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.alias").value("수정된 별칭"))
                    .andDo(document("address-update-put",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                            pathParameters(parameterWithName("address-id").description("수정할 주소 ID")),
                            requestFields(
                                    fieldWithPath("alias").description("별칭"),
                                    fieldWithPath("roadAddress").description("도로명 주소"),
                                    fieldWithPath("zipCode").description("우편번호"),
                                    fieldWithPath("addressDetail").description("상세 주소").optional()
                            ),
                            responseFields(withHeader(
                                    fieldWithPath("data.addressId").description("주소 ID"),
                                    fieldWithPath("data.alias").description("별칭"),
                                    fieldWithPath("data.roadAddress").description("도로명 주소"),
                                    fieldWithPath("data.zipCode").description("우편번호"),
                                    fieldWithPath("data.addressDetail").description("상세 주소")
                            ))
                    ));
        }
    }

    @Test
    @Order(5)
    @DisplayName("DELETE - 주소 삭제")
    void deleteAddress() throws Exception {
        Long userId = 1L;
        Long addressId = 10L;

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            mockMvc.perform(delete("/users/me/addresses/{address-id}", addressId)
                            .header("X-User-Id", userId))
                    .andExpect(status().isOk())
                    .andDo(document("address-delete",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                            pathParameters(parameterWithName("address-id").description("삭제할 주소 ID")),
                            responseFields(withHeader())
                    ));
        }
    }

    @Test
    @Order(6)
    @DisplayName("GET - 기본 주소 조회")
    void getDefaultAddress() throws Exception {
        Long userId = 1L;
        AddressResponse response = new AddressResponse(10L, "기본배송지", "도로명", "12345", "상세");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(addressService.getDefaultAddress(userId)).willReturn(response);

            mockMvc.perform(get("/users/me/addresses/default")
                            .header("X-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.alias").value("기본배송지"))
                    .andDo(document("address-default-get",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                            responseFields(withHeader(
                                    fieldWithPath("data.addressId").description("주소 ID"),
                                    fieldWithPath("data.alias").description("별칭"),
                                    fieldWithPath("data.roadAddress").description("도로명 주소"),
                                    fieldWithPath("data.zipCode").description("우편번호"),
                                    fieldWithPath("data.addressDetail").description("상세 주소")
                            ))
                    ));
        }
    }

    @Test
    @Order(7)
    @DisplayName("PUT - 기본 주소 설정 변경")
    void updateDefaultAddress() throws Exception {
        Long userId = 1L;
        Long addressId = 10L;

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            mockMvc.perform(put("/users/me/addresses/{address-id}/default", addressId)
                            .header("X-User-Id", userId))
                    .andExpect(status().isOk())
                    .andDo(document("address-set-default-put",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                            pathParameters(parameterWithName("address-id").description("기본 주소로 설정할 주소 ID")),
                            responseFields(withHeader())
                    ));
        }
    }

}