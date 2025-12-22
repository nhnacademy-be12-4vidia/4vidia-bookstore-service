package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.user.domain.Address;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserRole;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.CompleteProfileRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.ChangePasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.DeleteUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UpdateUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.OrderUserResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserInfoResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserProfileResponse;
import com.nhnacademy._vidiabookstoreservice.user.exception.IncorrectPasswordException;
import com.nhnacademy._vidiabookstoreservice.user.exception.invalid.PasswordMisMatchException;
import com.nhnacademy._vidiabookstoreservice.user.exception.invalid.SameAsOldPasswordException;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.UserNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("이메일로 회원 조회 성공")
    void getUserByEmail_success() {
        String email = "test@test.com";
        Long userId = 1L;
        String password = "encodedPassword";

        User user = mock(User.class);
        given(user.getUserId()).willReturn(userId);
        given(user.getEmail()).willReturn(email);
        given(user.getPassword()).willReturn(password);
        given(user.getRole()).willReturn(UserRole.USER);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        UserInfoResponse userInfo = userService.getUserByEmail(email);

        assertThat(userInfo.id()).isEqualTo(userId);
        assertThat(userInfo.email()).isEqualTo(email);
        assertThat(userInfo.password()).isEqualTo(password);
        assertThat(userInfo.roles()).isEqualTo(UserRole.USER.name());
    }

    @Test
    @DisplayName("이메일로 회원 조회 실패")
    void getUserByEmail_fail() {
        String email = "test@test.com";

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하는 회원정보가 없습니다.");
    }

    @Test
    @DisplayName("회원 포인트 반환 성공")
    void getUserByPoint_success() {
        Long userId = 1L;
        Integer point = 1000;

        User user = mock(User.class);
        given(user.getPoint()).willReturn(point);
        given(userRepository.findByUserId(userId)).willReturn(Optional.of(user));

        Integer userPoint = userService.getUserByPoint(userId);
        assertThat(userPoint).isEqualTo(point);
    }


    @Test
    @DisplayName("회원 포인트 반환 실패")
    void getUserByPoint_fail() {
        Long userId = 1L;

        given(userRepository.findByUserId(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByPoint(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하는 회원정보가 없습니다.");
    }

    @Test
    @DisplayName("회원 이름 조회 성공")
    void getUserName_success() {
        Long userId = 1L;
        String name = "테스트유저";

        User user = mock(User.class);
        given(user.getName()).willReturn(name);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        assertThat(userService.getUserName(userId)).isEqualTo(name);
    }

    @Test
    @DisplayName("회원 이름 조회 실패")
    void getUserName_fail() {
        Long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserName(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하는 회원정보가 없습니다.");
    }

    @Test
    @DisplayName("회원 주문 조회 성공")
    void getOrderUser_success() {
        Long userId = 1L;

        User user = mock(User.class);
        Address address = mock(Address.class);

        given(user.getEmail()).willReturn("test@test.com");
        given(user.getName()).willReturn("테스트유저");
        given(user.getPhone()).willReturn("01012345678");
        given(user.getPoint()).willReturn(5000);

        given(user.getAddress()).willReturn(address);
        given(address.getAddressId()).willReturn(100L);
        given(address.getAlias()).willReturn("우리집");
        given(address.getRoadAddress()).willReturn("광주광역시 지산동");
        given(address.getZipCode()).willReturn("12345");
        given(address.getAddressDetail()).willReturn("101");
        given(user.getAddresses()).willReturn(List.of());

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        OrderUserResponse orderUserResponse = userService.getOrderUser(userId);

        assertThat(orderUserResponse.email()).isEqualTo("test@test.com");
        assertThat(orderUserResponse.name()).isEqualTo("테스트유저");
        assertThat(orderUserResponse.point()).isEqualTo(5000);

        assertThat(orderUserResponse.addressId()).isEqualTo(100L);
        assertThat(orderUserResponse.roadAddress()).isEqualTo("광주광역시 지산동");
        assertThat(orderUserResponse.addressResponses()).isEmpty();
    }

    @Test
    @DisplayName("회원 주문 조회 실패")
    void getOrderUser_fail() {
        Long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getOrderUser(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하는 회원정보가 없습니다.");
    }

    @Test
    @DisplayName("회원정보 조회 성공")
    void getUserInfo_success() {
        Long userId = 1L;
        User user = mock(User.class);

        given(user.getUserId()).willReturn(userId);
        given(user.getEmail()).willReturn("test@test.com");
        given(user.getName()).willReturn("테스트유저");
        given(user.getPhone()).willReturn("01012345678");
        given(user.getBirthDate()).willReturn(LocalDate.of(1999, 8, 1));
        given(user.getPoint()).willReturn(5000);
        given(user.getAddress()).willReturn(mock(Address.class));
        given(user.getGrade()).willReturn(Grade.builder().gradeName(GradeName.WELCOME).build());

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        UserProfileResponse result = userService.getUserInfo(userId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo("test@test.com");
        assertThat(result.name()).isEqualTo("테스트유저");
        assertThat(result.phone()).isEqualTo("01012345678");
        assertThat(result.birthDate()).isEqualTo("1999-08-01");
        assertThat(result.point()).isEqualTo(5000);
        assertThat(result.gradeName()).isEqualTo("WELCOME");
    }

    @Test
    @DisplayName("회원정보 조회 실패")
    void getUserInfo_fail() {
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하는 회원정보가 없습니다.");
    }

    @Test
    @DisplayName("회원정보 수정 성공")
    void updateUserProfile_success() {
        Long userId = 1L;

        User user = User.builder()
                .name("테스트유저")
                .phone("01012345678")
                .grade(Grade.builder().gradeName(GradeName.WELCOME).build())
                .build();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        UpdateUserRequest updateUserRequest = mock(UpdateUserRequest.class);
        given(updateUserRequest.name()).willReturn("수정할 이름");
        given(updateUserRequest.phone()).willReturn("01011112222");

        UserProfileResponse result = userService.updateUserProfile(userId, updateUserRequest);
        assertThat(result.name()).isEqualTo("수정할 이름");
        assertThat(result.phone()).isEqualTo("01011112222");

        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("회원정보 수정 - 입력값이 없거나 공백일 경우 기존 정보 유지")
    void updateUserProfile_fail() {
        Long userId = 1L;

        User user = User.builder()
                .name("테스트유저")
                .phone("01012345678")
                .grade(Grade.builder().gradeName(GradeName.WELCOME).build())
                .build();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        UpdateUserRequest updateUserRequest = mock(UpdateUserRequest.class);
        given(updateUserRequest.name()).willReturn("");
        given(updateUserRequest.phone()).willReturn(null);

        UserProfileResponse result = userService.updateUserProfile(userId, updateUserRequest);
        assertThat(result.name()).isEqualTo("테스트유저");
        assertThat(result.phone()).isEqualTo("01012345678");

        assertThat(user.getName()).isEqualTo("테스트유저");
        assertThat(user.getPhone()).isEqualTo("01012345678");

        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("회원 프록시 조회 성공")
    void getProxyById_success() {
        Long userId = 1L;
        User user = mock(User.class);

        given(userRepository.getReferenceById(userId)).willReturn(user);

        User result = userService.getProxyById(userId);

        assertThat(result).isEqualTo(user);

        verify(userRepository, times(1)).getReferenceById(userId);
    }

    @Test
    @DisplayName("회원 PK조회 성공")
    void getUserById_success() {
        Long userId = 1L;
        User user = mock(User.class);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        User result = userService.getUserById(userId);

        assertThat(result).isEqualTo(user);

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("회원 PK조회 실패")
    void getUserById_fail() {
        Long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하는 회원정보가 없습니다.");
    }

    @Test
    @DisplayName("비밀번호 수정 성공")
    void changePassword_success() {
        Long userId = 1L;
        String currentPassword = "currentPassword1234!";
        String encodedOldPassword = bCryptPasswordEncoder.encode(currentPassword);

        String newPassword = "newPassword1234!";
        String encodedNewPassword = bCryptPasswordEncoder.encode(newPassword);

        ChangePasswordRequest request = new ChangePasswordRequest(
                currentPassword,
                newPassword,
                newPassword
        );

        User user = mock(User.class);
        given(user.getPassword()).willReturn(encodedOldPassword);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        given(bCryptPasswordEncoder.matches(currentPassword, encodedOldPassword))
                .willReturn(true);
        given(bCryptPasswordEncoder.matches(newPassword, encodedOldPassword))
                .willReturn(false);
        given(bCryptPasswordEncoder.encode(newPassword)).willReturn(encodedNewPassword);

        userService.changePassword(userId, request);

        verify(user, times(1)).updateEncodedPassword(encodedNewPassword);
    }

    @Test
    @DisplayName("비밀번호 수정 실패 - 유저 없음")
    void changePassword_fail_userNotFound() {
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword(userId, any()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하는 회원정보가 없습니다.");
    }

    @Test
    @DisplayName("비밀번호 수정 실패 - 현재 비밀번호 불일치")
    void changePassword_fail_incorrectPassword() {
        Long userId = 1L;
        String currentPassword = "wrongPassword";
        String encodedOldPassword = bCryptPasswordEncoder.encode(currentPassword);

        ChangePasswordRequest request = new ChangePasswordRequest(
                currentPassword,
                "newPassword",
                "newPassword"
        );

        User user = mock(User.class);
        given(user.getPassword()).willReturn(encodedOldPassword);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        given(bCryptPasswordEncoder.matches(currentPassword, encodedOldPassword))
                .willReturn(false);

        assertThatThrownBy(() -> userService.changePassword(userId, request))
                .isInstanceOf(IncorrectPasswordException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("비밀번호 수정 실패 - 새 비밀번호와 확인 비밀번호 불일치")
    void changePassword_fail_passwordMismatch() {
        Long userId = 1L;
        String currentPassword = "currentPassword1234!";
        String encodedOldPassword = bCryptPasswordEncoder.encode(currentPassword);

        ChangePasswordRequest request = new ChangePasswordRequest(
                currentPassword,
                "newPassword",
                "newDiffPassword"
        );

        User user = mock(User.class);
        given(user.getPassword()).willReturn(encodedOldPassword);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        given(bCryptPasswordEncoder.matches(currentPassword, encodedOldPassword))
                .willReturn(true);

        assertThatThrownBy(() -> userService.changePassword(userId, request))
                .isInstanceOf(PasswordMisMatchException.class)
                .hasMessageContaining("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("비밀번호 수정 실패 - 새 비밀번호가 기존 비밀번호와 동일")
    void changePassword_fail_sameAsOldPassword() {
        Long userId = 1L;
        String currentPassword = "currentPassword1234!";
        String encodedOldPassword = bCryptPasswordEncoder.encode(currentPassword);

        String newPassword = "currentPassword1234!";

        ChangePasswordRequest request = new ChangePasswordRequest(
                currentPassword,
                newPassword,
                newPassword
        );

        User user = mock(User.class);
        given(user.getPassword()).willReturn(encodedOldPassword);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        given(bCryptPasswordEncoder.matches(currentPassword, encodedOldPassword))
                .willReturn(true);
        given(bCryptPasswordEncoder.matches(newPassword, encodedOldPassword))
                .willReturn(true);

        assertThatThrownBy(() -> userService.changePassword(userId, request))
                .isInstanceOf(SameAsOldPasswordException.class)
                .hasMessageContaining("현재 비밀번호와 동일한 비밀번호로는 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("회원탈퇴 성공")
    void deleteUserById_success() {
        Long userId = 1L;
        String currentPassword = "currentPassword1234!";
        String encodedPassword = bCryptPasswordEncoder.encode(currentPassword);

        DeleteUserRequest request = new DeleteUserRequest(currentPassword);

        User user = User.builder()
                .password(encodedPassword)
                .build();
        user.setStatus(UserStatus.ACTIVE);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(bCryptPasswordEncoder.matches(request.currentPassword(), user.getPassword())).willReturn(true);

        userService.deleteUserById(userId, request);

        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 유저 없음")
    void deleteUserById_fail_userNotFound() {
        Long userId = 1L;

        DeleteUserRequest request = new DeleteUserRequest("currentPassword");

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUserById(userId, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하는 회원정보가 없습니다.");

        verifyNoInteractions(bCryptPasswordEncoder);
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 비밀번호 불일치")
    void deleteUserById_fail_incorrectPassword() {
        Long userId = 1L;
        String currentPassword = "wrongPassword1234!";
        String encodedPassword = bCryptPasswordEncoder.encode(currentPassword);

        DeleteUserRequest request = new DeleteUserRequest(currentPassword);

        User user = mock(User.class);
        given(user.getPassword()).willReturn(encodedPassword);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        given(bCryptPasswordEncoder.matches(currentPassword, encodedPassword)).willReturn(false);

        assertThatThrownBy(() -> userService.deleteUserById(userId, request))
                .isInstanceOf(IncorrectPasswordException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다.");

        verify(user, never()).setStatus(any());
    }

    @Test
    @DisplayName("마지막 로그인 시간 업데이트 성공")
    void updateLastLoginAt_success() {
        String email = "test@test.com";

        LocalDateTime oldTime = LocalDateTime.of(2025, 12, 20, 10, 42, 22);

        User user = User.builder()
                .email(email)
                .build();
        user.setLastLoginAt(oldTime);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.updateLastLoginAt(email);

        // 기존 시간과 다른지 확인
        assertThat(user.getLastLoginAt()).isNotEqualTo(oldTime);

        // 현재ㅅ간 기준으로 1초 이내인지 확인 (오차범위 1초)
        assertThat(user.getLastLoginAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("마지막 로그인 시간 업데이트 실패")
    void updateLastLoginAt_fail() {
        String email = "test@test.com";

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateLastLoginAt(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하는 회원정보가 없습니다.");
    }

    @Test
    @DisplayName("회원 권한 조회 성공")
    void getUserRole_success() {
        Long userId = 1L;

        User user = User.builder().build();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        String userRole = userService.getUserRole(userId);
        assertThat(userRole).isEqualTo(UserRole.USER.name());
    }

    @Test
    @DisplayName("회원 권한 조회 실패")
    void getUserRole_fail() {
        Long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserRole(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하는 회원정보가 없습니다.");
    }

    @Test
    @DisplayName("payco 로그인 성공 - 프로필 완성 및 회원 활성화")
    void completeProfile_success() {
        Long userId = 1L;

        User user = User.builder()
                .build();
        user.setStatus(UserStatus.TEMP);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        CompleteProfileRequest request = new CompleteProfileRequest(
                "test@test.com",
                "테스트 유저",
                "01012345678",
                LocalDate.of(1998, 4, 12)
        );

        userService.completeProfile(userId, request);

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getEmail()).isEqualTo("test@test.com");
        assertThat(user.getName()).isEqualTo("테스트 유저");
        assertThat(user.getPhone()).isEqualTo("01012345678");
        assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(1998, 4, 12));
    }

    @Test
    @DisplayName("payco 로그인 실패 - 유저 없음")
    void completeProfile_fail() {
        Long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.completeProfile(userId, any()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하는 회원정보가 없습니다.");
    }
}