package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.request.AddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.request.CreateAddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.repository.AddressRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureRestDocs
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@ActiveProfiles("local")
@SpringBootTest
@Transactional
class AddressControllerTest {


    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AddressRepository addressRepository;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("[주소 등록]")
    void registerAddress() throws Exception {
        Long userId = 14L;

        CreateAddressRequest request = new CreateAddressRequest(
                "우리집",
                "광주광역시 동구 조선대5길 65 (서석동)",
                "61452",
                "101"
        );

        mockMvc.perform(post("/users/me/addresses")
                        .header("X-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(document("user-address-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        requestFields(
                                fieldWithPath("alias").description("등록할 별칭"),
                                fieldWithPath("roadAddress").description("등록할 도로명 주소"),
                                fieldWithPath("zipCode").description("등록할 우편번호"),
                                fieldWithPath("addressDetail").description("등록할 상세주소")
                        )
                ));
    }

    @Test
    @DisplayName("[주소 단일 조회]")
    void getAddress() throws Exception {
        Long userId = 14L;
        Long addressId = addressRepository.findAllByUser_UserId(userId).getFirst().getAddressId();

        mockMvc.perform(get("/users/me/addresses/{address-id}", addressId)
                        .header("X-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("addressId").isNumber())
                .andExpect(jsonPath("alias").isString())
                .andExpect(jsonPath("roadAddress").isString())
                .andExpect(jsonPath("zipCode").isString())
                .andExpect(jsonPath("addressDetail").isString())
                .andDo(document("user-address-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        pathParameters(
                                parameterWithName("address-id").description("조회할 주소 ID")
                        ),
                        responseFields(
                                fieldWithPath("addressId").description("주소 ID"),
                                fieldWithPath("alias").description("별칭"),
                                fieldWithPath("roadAddress").description("도로명 주소"),
                                fieldWithPath("zipCode").description("우편번호"),
                                fieldWithPath("addressDetail").description("상세주소")
                        )
                ));
    }

    @Test
    @DisplayName("[주소 전체 조회]")
    void getAddressList() throws Exception {
        Long userId = 14L;

        mockMvc.perform(get("/users/me/addresses")
                        .header("X-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("user-addresses-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].addressId").description("주소 ID"),
                                fieldWithPath("[].alias").description("별칭"),
                                fieldWithPath("[].roadAddress").description("도로명 주소"),
                                fieldWithPath("[].zipCode").description("우편번호"),
                                fieldWithPath("[].addressDetail").description("상세주소")
                        )
                ));
    }

    @Test
    @DisplayName("[기본주소 조회]")
    void getDefaultAddress() throws Exception {
        Long userId = 14L;

        mockMvc.perform(get("/users/me/addresses/default")
                        .header("X-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("user-address-default-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        responseFields(
                                fieldWithPath("addressId").description("주소 ID"),
                                fieldWithPath("alias").description("기본주소 별칭"),
                                fieldWithPath("roadAddress").description("기본주소 도로명 주소"),
                                fieldWithPath("zipCode").description("기본주소 우편번호"),
                                fieldWithPath("addressDetail").description("기본주소 상세주소")
                        )
                ));
    }

    @Test
    @DisplayName("[주소 수정]")
    void updateAddress() throws Exception {
        Long userId = 14L;
        Long addressId = addressRepository.findAllByUser_UserId(userId).getLast().getAddressId();

        AddressRequest request = new AddressRequest(
                "본가",
                "서울특별시 구로구 디지털로26길 72 (구로동)",
                "07071",
                "3005"
        );

        mockMvc.perform(put("/users/me/addresses/{address-id}", addressId)
                        .header("X-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("alias").isString())
                .andExpect(jsonPath("roadAddress").isString())
                .andExpect(jsonPath("zipCode").isString())
                .andExpect(jsonPath("addressDetail").isString())
                .andDo(document("user-address-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        pathParameters(
                                parameterWithName("address-id").description("수정할 주소 ID")
                        ),
                        requestFields(
                                fieldWithPath("alias").description("수정할 별칭"),
                                fieldWithPath("roadAddress").description("수정할 도로명 주소"),
                                fieldWithPath("zipCode").description("수정할 우편번호"),
                                fieldWithPath("addressDetail").description("수정할 상세주소")
                        ),
                        responseFields(
                                fieldWithPath("addressId").description("주소 ID"),
                                fieldWithPath("alias").description("수정된 별칭"),
                                fieldWithPath("roadAddress").description("수정된 도로명 주소"),
                                fieldWithPath("zipCode").description("수정된 우편번호"),
                                fieldWithPath("addressDetail").description("수정된 상세주소")
                        )
                ));
    }

    @Test
    @DisplayName("[기본주소 변경]")
    void updateDefaultAddress() throws Exception {
        Long userId = 14L;
        Long addressId = addressRepository.findAllByUser_UserId(userId).getLast().getAddressId();

        mockMvc.perform(put("/users/me/addresses/{address-id}/default", addressId)
                        .header("X-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(document("user-address-default-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        pathParameters(
                                parameterWithName("address-id").description("기본주소로 변경할 주소 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[주소 삭제]")
    void deleteAddress() throws Exception {
        Long userId = 14L;
        Long addressId = addressRepository.findAllByUser_UserId(userId).getLast().getAddressId();

        mockMvc.perform(delete("/users/me/addresses/{address-id}", addressId)
                        .header("X-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("user-address-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        pathParameters(
                                parameterWithName("address-id").description("삭제할 주소 ID")
                        )
                ));
    }
}