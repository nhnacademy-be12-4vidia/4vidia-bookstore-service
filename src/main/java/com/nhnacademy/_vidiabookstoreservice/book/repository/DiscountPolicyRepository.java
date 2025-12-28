package com.nhnacademy._vidiabookstoreservice.book.repository;

import com.nhnacademy._vidiabookstoreservice.book.domain.DiscountPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DiscountPolicyRepository extends JpaRepository<DiscountPolicy, Long> {

    // 특정 카테고리의 유효한 할인 정책 조회
    @Query("SELECT dp FROM DiscountPolicy dp " +
           "WHERE dp.category.id = :categoryId " +
           "AND :today BETWEEN dp.startDate AND dp.endDate")
    Optional<DiscountPolicy> findActivePolicyByCategoryId(@Param("categoryId") Long categoryId, @Param("today") LocalDate today);

    // 전역(기본) 할인 정책 조회 (categoryId is null)
    @Query("SELECT dp FROM DiscountPolicy dp " +
           "WHERE dp.category IS NULL " +
           "AND :today BETWEEN dp.startDate AND dp.endDate")
    Optional<DiscountPolicy> findActiveGlobalPolicy(@Param("today") LocalDate today);
}
