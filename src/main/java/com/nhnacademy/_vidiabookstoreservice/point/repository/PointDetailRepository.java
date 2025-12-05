package com.nhnacademy._vidiabookstoreservice.point.repository;


import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointDetailRepository extends JpaRepository<PointDetail, Long> {
    /**
     * 사용 가능한 포인트 목록 조회
     * -특정 유저의 적립 포인트 중
     * -price>0 (적립된 포인트)
     * -expiredAt> now (아직 소멸되지 않은)
     * -expiredAt ASC (유효기간이 짧은 것부터 사용하기 위해 오름차순)
     */
    @Query("SELECT p FROM PointDetail p " +
            "WHERE p.userId = :userId " +
            "AND p.price > 0 " +
            "AND p.expiredAt > :now " +
            "ORDER BY p.expiredAt ASC")
    List<PointDetail> findAvailablePointForUse(@Param("userId")Long userId,
                                               @Param("now")LocalDateTime now);

    /**
     * 현재 보유 포인트 총합 조회
     */
    @Query("SELECT COALESCE(SUM(p.price), 0) FROM PointDetail p " +
            "WHERE p.userId = :userId")
    int getRemainPoint(@Param("userId") Long userId);

    /**
     * 소멸 대상 포인트 조회 ( 배치 스케줄러용)
     */
    @Query("SELECT p FROM PointDetail p " +
            "WHERE p.price > 0 " +
            "AND p.expiredAt < :now")
    List<PointDetail> findExpiredPoints(@Param("now") LocalDateTime now);

    /**
     *곧 소멸 예정인 포인트 조회
     * now < expiredAt <= limit
     */
    @Query("SELECT COALESCE(SUM(p.price), 0) FROM PointDetail p " +
            "WHERE p.userId = :userId " +
            "AND p.price > 0 " +
            "AND p.expiredAt > :now " +
            "AND p.expiredAt <= :limit")
    int getExpiringSoon(@Param("userId") Long userId,
                        @Param("now") LocalDateTime now,
                        @Param("limit") LocalDateTime limit);

    /**
     * 포인트 내역 최신 순 조회
     */

    Page<PointDetail> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);



    /**
     * 중복 환불 방지 체크
     * 동일 orderId + 동일 Reason 로 이미 환불 이력이 있으면 true
     * 환불 API 호출 시 중복 환불 차단에 필수
     */
    boolean existsByUserIdAndOrderIdAndReason(Long userId, Long orderId, PointReason reason);

    List<PointDetail> findByOrderId(Long orderId);
}
