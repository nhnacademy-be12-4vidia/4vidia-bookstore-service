package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindIdRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindPasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.LoginRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UserSignupRequest;
import com.nhnacademy._vidiabookstoreservice.user.exception.*;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.AuthService;
import com.nhnacademy._vidiabookstoreservice.user.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final EmailService mailService;
    private final BCryptPasswordEncoder BCryptPasswordEncoder;

    /**
     * 회원가입
     */
    @Override
    public Long register(UserSignupRequest request) {
        if(userRepository.existsByEmail(request.email())){
            throw new UserAlreadyExistsException(request.email());
        }

        Grade defaultGrade = gradeRepository.findByGradeName(GradeName.WELCOME);

        // User 생성
        User user = User.builder()
                .email(request.email())
                .password(BCryptPasswordEncoder.encode(request.password()))
                .name(request.name())
                .phone(request.phone())
                .birthDate(request.birthDate())
                .grade(defaultGrade)
                .build();
        log.info("grade : {}", user.getGrade());

        userRepository.save(user);

        return user.getUserId();
    }

    /**
     * 아이디 찾기 (이름 + 생일 + 전화번호)
     */
    @Override
    public String findUserId(FindIdRequest request) {
        LocalDate birthday = LocalDate.parse(request.birthday());
        User user = userRepository.findByNameAndBirthDateAndPhone(
                        request.name(),birthday,request.phone()
                )
                .orElseThrow(()-> new UserNotFoundException());
        return user.getEmail();
    }

    /**
     * 비밀번호 찾기 ( 아이디 + 이름 + 전화번호) -> 임시 비밀번호 발급
     */
    @Override
    public String restPasswordAndSendMail (FindPasswordRequest request) {
        User user = userRepository.findByEmailAndNameAndPhone(
                request.email(),request.name(),request.phone()
        ).orElseThrow(()-> new UserNotFoundByEmailException(request.email()));

        // 임시 비밀번호 생성
        String tempPassword = generateTempPassword(10);

        // 비밀번호 암호화 후 저장

        String encodedPassword = BCryptPasswordEncoder.encode(tempPassword);
        user.updateEncodedPassword(encodedPassword);
        userRepository.save(user);

        // 이메일 발송 ( 구체 로직은 MailService에서)
        mailService.sendTempPassword(user.getEmail(), tempPassword);
        return "임시 비밀번호가 이메일로 발송되었습니다.";

    }

    // 이메일 중복 체크
    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // 임시 비밀번호 발급 로직
    private String generateTempPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }


    // 로그인 아이디,비번 체크 -> 맞으면 -> 회원상태 확인 후 휴먼상태 여부 보내기
    @Override
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

        // 4. 장기 미접속때 -> 업데이트해줘야함 // todo : 스케줄러 사용해야함 -> 배치...?
        if (user.isDormant()) {
            user.setStatus(UserStatus.DORMANT);
            userRepository.save(user);
            return true; // 휴먼 ㅇㅇ (이때도 마지막로그인 업데이트 안함)
        }

        return false; // 휴먼 ㄴㄴ
    }

}
