package com.nhnacademy._vidiabookstoreservice.user.service.impl;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.dto.grade.response.GradePolicyResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.grade.response.GradeResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.UserNetSum;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.GradeNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.UserNotFoundException;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
    @Transactional(readOnly = true)
    public GradeResponse getGrade(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

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
                .orElseThrow(() -> new UserNotFoundException(userId));
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new GradeNotFoundException(gradeId));

        user.setGrade(grade);
    }

    /**
     * 월간 등급 산정 (최근 3개월 순수 주문금액 기준)
     * - 스케줄러/수동 실행 어디서든 호출 가능하도록 서비스로 분리
     */
//    @Transactional
    public int recalculateMonthlyGrades() {
        ZoneId zone = ZoneId.of("Asia/Seoul");

        // 이번달 1일 00:00 ~ 3개월 전 1일 00:00
        YearMonth ym = YearMonth.now(zone);
        LocalDateTime to = ym.atDay(1).atStartOfDay();
        LocalDateTime from = ym.minusMonths(3).atDay(1).atStartOfDay();

        // 테스트용
//        LocalDateTime to = LocalDateTime.now(zone);
//        LocalDateTime from = to.minusMonths(3)
//                .withDayOfMonth(1)
//                .toLocalDate()
//                .atStartOfDay();


        // 유저별 순수금액 집계 (주문한 유저만 결과가 옴)
        List<UserNetSum> rows = orderRepository.findUserNetSumLast3Months(from, to, PointReason.ORDER_CANCEL_REFUND);
        Map<Long, Long> netMap = rows.stream()
                .collect(Collectors.toMap(UserNetSum::getUserId, UserNetSum::getNetSum));

        // Grade 엔티티 미리 로딩(GradeName -> Grade)
        Map<GradeName, Grade> gradeMap = loadGradeMap();

        int page = 0;
        int size = 1000;
        int totalUpdated = 0;

        while (true) {
            Page<User> userPage = userRepository.findAll(PageRequest.of(page, size));
            if (userPage.isEmpty()) break;

            for (User user : userPage.getContent()) {
                long netSum = netMap.getOrDefault(user.getUserId(), 0L);

                GradeName newGradeName = decide(netSum);
                Grade newGrade = gradeMap.get(newGradeName);

                Grade current = user.getGrade();
                boolean changed = (current == null) || (current.getGradeName() != newGradeName);

                if (changed) {
                    user.setGrade(newGrade);
                    totalUpdated++;
                }
            }

            em.flush();
            em.clear();
            page++;
        }

        log.info("[GradeService] 기간: {} ~ {}", from, to);
        log.info("[GradeService] 등급 변경: {}명", totalUpdated);

        return totalUpdated;
    }

    private Map<GradeName, Grade> loadGradeMap() {
        Map<GradeName, Grade> gradeMap = new EnumMap<>(GradeName.class);
        for (GradeName gn : GradeName.values()) {
            Grade grade = gradeRepository.findByGradeName(gn);
            if (grade == null) {
                throw new GradeNotFoundException(gn);
            }
            gradeMap.put(gn, grade);
        }
        return gradeMap;
    }

    private GradeName decide(long netSum) {
        if (netSum >= 400_000) return GradeName.PLATINUM;
        if (netSum >= 300_000) return GradeName.GOLD;
        if (netSum >= 200_000) return GradeName.ROYAL;
        if (netSum >= 100_000) return GradeName.REGULAR;
        return GradeName.WELCOME;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradePolicyResponse> getGradePolicies() {
        // GradeName 순서대로(코드 기준)
        return List.of(
                policy(GradeName.WELCOME, 0L, 99_999L, "기본 등급"),
                policy(GradeName.REGULAR, 100_000L, 199_999L, "순수주문금액 10만원 이상"),
                policy(GradeName.ROYAL, 200_000L, 299_999L, "순수주문금액 20만원 이상"),
                policy(GradeName.GOLD, 300_000L, 399_999L, "순수주문금액 30만원 이상"),
                policy(GradeName.PLATINUM, 400_000L, null, "순수주문금액 40만원 이상")
        );
    }

    private GradePolicyResponse policy(GradeName name, Long min, Long max, String desc) {
        Grade grade = gradeRepository.findByGradeName(name);
        if (grade == null) throw new GradeNotFoundException(name);

        return GradePolicyResponse.builder()
                .gradeName(name.name())
                .pointRate(grade.getPointRate()) // ✅ DB에서 가져온 최신 적립률
                .minNetAmount(min)
                .maxNetAmount(max)
                .desc(desc)
                .build();
    }






}
