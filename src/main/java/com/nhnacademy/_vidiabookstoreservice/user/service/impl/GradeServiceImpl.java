package com.nhnacademy._vidiabookstoreservice.user.service.impl;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.dto.grade.response.GradeResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.UserNetSum;
import com.nhnacademy._vidiabookstoreservice.user.exception.GradeNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserNotFoundByUserIdException;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.GradeService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GradeServiceImpl implements GradeService {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final OrderRepository orderRepository;
    private final EntityManager em;

    /**
     * 등급 조회
     */
    @Override
    public GradeResponse getGrade(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundByUserIdException(userId));

        return GradeResponse.builder()
                .gradeName(user.getGrade().getGradeName().name())
                .pointRate(user.getGrade().getPointRate())
                .build();
    }

    /**
     * 등급 변경
     */
    @Override
    public void updateGrade(Long userId, Long gradeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundByUserIdException(userId));
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new GradeNotFoundException(gradeId));

        user.setGrade(grade);
    }

    /**
     * 월간 등급 산정 스케줄러 로직
     */
    @Override
    public void updateUserGradesMonthly() {
        long startTime = System.currentTimeMillis();
        ZoneId zone = ZoneId.of("Asia/Seoul");

        // 날짜 설정
        // LocalDateTime to = LocalDateTime.now().plusDays(1); // 테스트용
        // LocalDateTime from = LocalDateTime.now().minusMonths(3);
        YearMonth ym = YearMonth.now(zone);
        LocalDateTime to = ym.atDay(1).atStartOfDay();
        LocalDateTime from = ym.minusMonths(3).atDay(1).atStartOfDay();

        // [수정] 파라미터에 '구매 확정(CONFIRMED)' 상태값 추가
        int confirmedCode = ConfirmStatus.CONFIRMED.getCode(); //

        // 1. 3개월간 순수 주문 금액 집계 (쿠폰 차감된 버전)
        List<UserNetSum> rows = orderRepository.findUserNetSumLast3Months(from, to, confirmedCode);
        Map<Long, Long> netMap = rows.stream()
                .collect(Collectors.toMap(UserNetSum::userId, UserNetSum::netSum));

        // 2. 등급 정보 로딩 (기존 로직 유지)
        Map<GradeName, Grade> gradeMap = EnumSet.allOf(GradeName.class).stream()
                .collect(Collectors.toMap(g -> g, gradeRepository::findByGradeName));

        int totalUpdatedCount = 0;
        int page = 0;
        int size = 1000; // 한번에 1000명씩 조회

        // 3. [변경 핵심] 주문한 사람이 아니라 '모든 유저'를 대상으로 반복
        while (true) {
            Page<User> userPage = userRepository.findAll(PageRequest.of(page, size));
            List<User> users = userPage.getContent();

            if (users.isEmpty()) {
                break; // 더 이상 유저가 없으면 종료
            }

            for (User user : users) {
                Long uid = user.getUserId();

                // 구매 내역이 있으면 그 금액, 없으면 0원! (이제 강등 가능)
                long netSum = netMap.getOrDefault(uid, 0L);

                GradeName newGradeName = decide(netSum);
                Grade newGrade = gradeMap.get(newGradeName);

                if (user.getGrade() == null || user.getGrade().getGradeName() != newGradeName) {
                    user.setGrade(newGrade);
                    totalUpdatedCount++;
                }
            }

            em.flush();
            em.clear();
            page++; // 다음 페이지로
        }

        long endTime = System.currentTimeMillis();
        // 로그 출력 (기존 동일)
        log.info("========== 월간 등급 산정 완료 ==========");
        log.info("등급 변경됨: {}명", totalUpdatedCount);
        log.info("소요 시간: {}ms", (endTime - startTime));
    }
    public static GradeName decide(long netSum){
        if(netSum >=400_000){ return GradeName.PLATINUM;}
        if(netSum>=300_000){ return GradeName.GOLD;}
        if(netSum>=200_000){ return GradeName.ROYAL;}
        if(netSum>=100_000){ return GradeName.REGULAR;}
        return GradeName.WELCOME;
    }




}
