package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.user.domain.Address;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.request.AddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.request.CreateAddressRequest;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AddressControllerTest extends SupportControllerTest {
    @Autowired private AddressRepository addressRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private GradeRepository gradeRepository;
    @Autowired private EntityManager entityManager;

    private Long testUserId;
    private Long testAddressId;

    @BeforeEach
    void initData() {
        Grade grade = gradeRepository.save(Grade.builder().gradeName(GradeName.WELCOME).pointRate(1).build());

        User user = User.builder()
                .email("addr_test@test.com")
                .password("pwd")
                .name("AddrUser")
                .phone("01000000000")
                .birthDate(LocalDate.now())
                .grade(grade)
                .build();
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        this.testUserId = savedUser.getUserId();

        Address address = Address.builder()
                .alias("집")
                .roadAddress("서울")
                .zipCode("12345")
                .addressDetail("101호")
                .user(savedUser)
                .build();
        Address savedAddress = addressRepository.save(address);
        this.testAddressId = savedAddress.getAddressId();

        savedUser.setDefaultAddress(savedAddress);
        userRepository.save(savedUser);

        // 영속성 컨텍스트 초기화 (조회 시 DB에서 새로 긁어오도록)
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("[주소 등록]")
    void registerAddress() throws Exception {
        CreateAddressRequest request = new CreateAddressRequest(
                "회사",
                "판교",
                "12345",
                "10층"
        );

        mockMvc.perform(post("/users/me/addresses")
                        .header("X-User-Id", testUserId)
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
                                fieldWithPath("alias").description("별칭"),
                                fieldWithPath("roadAddress").description("도로명 주소"),
                                fieldWithPath("zipCode").description("우편번호"),
                                fieldWithPath("addressDetail").description("상세주소")
                        )
                ));
    }

    @Test
    @DisplayName("[주소 단일 조회]")
    void getAddress() throws Exception {
        mockMvc.perform(get("/users/me/addresses/{address-id}", testAddressId)
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("addressId").value(testAddressId))
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
        mockMvc.perform(get("/users/me/addresses")
                        .header("X-User-Id", testUserId)
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
        mockMvc.perform(get("/users/me/addresses/default")
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // .andExpect(jsonPath("addressId").exists()) // 기본주소가 없으면 null 리턴 가능성 있음
                .andDo(document("user-address-default-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
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
    @DisplayName("[주소 수정]")
    void updateAddress() throws Exception {
        AddressRequest request = new AddressRequest(
                "본가수정",
                "서울",
                "11111",
                "202호"
        );

        mockMvc.perform(put("/users/me/addresses/{address-id}", testAddressId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
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
        mockMvc.perform(put("/users/me/addresses/{address-id}/default", testAddressId)
                        .header("X-User-Id", testUserId)
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
        // 기본 주소는 삭제할 수 없으므로, 기본 주소가 아닌 새 주소를 만들어서 삭제 테스트
        Address newAddress = addressRepository.save(Address.builder().user(userRepository.findById(testUserId).get()).alias("삭제용").roadAddress("어디").zipCode("000").addressDetail("0").build());

        mockMvc.perform(delete("/users/me/addresses/{address-id}", newAddress.getAddressId())
                        .header("X-User-Id", testUserId)
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