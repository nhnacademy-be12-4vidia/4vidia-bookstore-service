package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.global.client.CouponClient;
import com.nhnacademy._vidiabookstoreservice.order.dto.event.BestSellerUpdateEvent;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointPolicyRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserRole;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindIdRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindPasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.PaycoUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.UserSignupRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.response.OAuth2UserDto;
import com.nhnacademy._vidiabookstoreservice.user.dto.event.WelcomeCouponIssueEvent;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserInfoResponse;
import com.nhnacademy._vidiabookstoreservice.user.exception.*;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.AuthService;
import com.nhnacademy._vidiabookstoreservice.user.service.EmailService;
import com.nhnacademy._vidiabookstoreservice.user.service.event.WelcomeCouponEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final EmailService mailService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
//    private final CouponClient couponClient;
    private final PointCommandService pointCommandService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 회원가입
     */
    @Override
//    @Transactional // 이거 없으면 롤백이 안됩니다요 (근데 지금 생일쿠폰 호출 오류나서 transaction 있으면 회원가입 안됨.. 쿠폰 호출 주석처리 하세요.. )
    public Long register(UserSignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        Grade defaultGrade = gradeRepository.findByGradeName(GradeName.WELCOME);

        // User 생성
        User user = User.builder()
                .email(request.email())
                .password(bCryptPasswordEncoder.encode(request.password()))
                .name(request.name())
                .phone(request.phone())
                .birthDate(request.birthDate())
                .grade(defaultGrade)
                .build();
        log.info("grade : {}", user.getGrade());

        userRepository.save(user);
        pointCommandService.rewardByPolicy(new PointPolicyRewardRequest(user.getUserId(), 1L));

//        couponClient.getRegisterCoupon(user.getUserId()); // todo : 분리
        eventPublisher.publishEvent(new WelcomeCouponIssueEvent(user.getUserId()));

        //TODO 생일 달인지 체크해서 맞으면? 생일쿠폰 요청

        return user.getUserId();
    }

    /**
     * 아이디 찾기 (이름 + 생일 + 전화번호)
     */
    @Override
    @Transactional(readOnly = true)
    public String findUserId(FindIdRequest request) {
        LocalDate birthday = LocalDate.parse(request.birthday());
        User user = userRepository.findByNameAndBirthDateAndPhone(
                        request.name(), birthday, request.phone()
                )
                .orElseThrow(UserNotFoundException::new);
        return user.getEmail();
    }

    /**
     * 비밀번호 찾기 ( 아이디 + 이름 + 전화번호) -> 임시 비밀번호 발급
     */
    @Override
    public String restPasswordAndSendMail(FindPasswordRequest request) {
        User user = userRepository.findByEmailAndNameAndPhone(
                request.email(), request.name(), request.phone()
        ).orElseThrow(() -> new UserNotFoundByEmailException(request.email()));

        // 임시 비밀번호 생성
        String tempPassword = generateTempPassword(10);

        // 비밀번호 암호화 후 저장

        String encodedPassword = bCryptPasswordEncoder.encode(tempPassword);
        user.updateEncodedPassword(encodedPassword);
        userRepository.save(user);

        // 이메일 발송 ( 구체 로직은 MailService에서)
        mailService.sendTempPassword(user.getEmail(), tempPassword);
        return "임시 비밀번호가 이메일로 발송되었습니다.";

    }

    // 이메일 중복 체크
    @Override
    @Transactional(readOnly = true)
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // 임시 비밀번호 발급 로직
    private String generateTempPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }


    // 로그인 아이디,비번 체크 -> 맞으면 -> 회원상태 확인 후 휴먼상태 여부 보내기
    @Override
    @Transactional(readOnly = true)
    public Boolean isDormant(String email) {

        // 1. 이메일로 회원 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundByEmailException(email));

        // 2. 회원탈퇴한 이메일은 -> 예외 던짐
        if (user.getStatus().equals(UserStatus.DELETED)) {
            throw new AlreadyResignedUserException(email);
        }

        // 3. 이미 휴먼일때 (dormant 상태일때)
        if (user.getStatus().equals(UserStatus.DORMANT)) {
            // 마지막 로그인 업데이트 안하고 바로 리턴
            return true;
        }


        return false; // 휴먼 ㄴㄴ
    }

    @Transactional
    public int convertDormantUsers(LocalDateTime day) {
        List<User> targets =
                userRepository.findActiveUsersNotLoggedInSince(UserStatus.ACTIVE, day);
        targets.forEach(
                user ->
                        user.setStatus(UserStatus.DORMANT)
        );

        return targets.size(); // 처리 건수 로깅용
    }

    @Override
    public OAuth2UserDto findOrCreateOAuthUser(String provider, PaycoUserRequest paycoUserRequest) {
        User user = userRepository.findByProviderAndSocialId(provider, paycoUserRequest.id()).orElseGet(
                () -> createTempOAuthUser(provider, paycoUserRequest)
        );
        return OAuth2UserDto.fromEntity(user);
    }

    private User createTempOAuthUser(String provider, PaycoUserRequest paycoUserRequest) {
        String name = "PAYCO임시이름";
        String email = provider + "_" + paycoUserRequest.id() + "@temp.4vidia.shop";
        String phone = "01012345678";
        Grade defaultGrade = gradeRepository.findByGradeName(GradeName.WELCOME);

        String rawPassword = java.util.UUID.randomUUID().toString();
        String encodedPassword = bCryptPasswordEncoder.encode(rawPassword);
        User user = User.oauthBuilder()
                .provider(provider)
                .socialId(paycoUserRequest.id())
                .email(email)
                .role(UserRole.USER)
                .status(UserStatus.TEMP)
                .name(name)
                .phone(phone)
                .password(encodedPassword)
                .grade(defaultGrade)
                .build();

        return userRepository.save(user);
    }

}
