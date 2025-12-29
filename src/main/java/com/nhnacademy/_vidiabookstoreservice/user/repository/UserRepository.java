package com.nhnacademy._vidiabookstoreservice.user.repository;

import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    // 아이디 찾기
    Optional<User> findByNameAndBirthDateAndPhone(String name, LocalDate birthday, String phone);
    // 비밀번호 찾기
    Optional<User> findByEmailAndNameAndPhone(String email, String name, String phone);

    Optional<User> findByEmail(String email);

    // 단순조회용
    Optional<User> findByUserId(Long userId);

    //동시성 제어용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.userId = :userId")
    Optional<User> findByUserIdWithLock(Long userId);


    // 3개월 이전 + active 사용자 조회
        @Query("""
    select u
    from User u
    where u.status = :status
      and (
          (u.lastLoginAt is not null and u.lastLoginAt < :threshold)
          or
          (u.lastLoginAt is null and cast(u.createdAt as localdatetime) < :threshold)
      )
    """)
    List<User> findActiveUsersToDormant(@Param("status") UserStatus status,
                                        @Param("threshold") LocalDateTime threshold);


    Optional<User> findByProviderAndSocialId(String provider, String socialId);

    //관리자 페이지  회원 검색용
    @Query("""
        select u from User u
        where (:status is null or u.status = :status)
          and (
                :keyword is null
                or lower(u.email) like lower(concat('%', :keyword, '%'))
                or lower(u.name) like lower(concat('%', :keyword, '%'))
                or u.phone like concat('%', :keyword, '%')
          )
        """)
    Page<User> searchAdminUsers(@Param("status") UserStatus status, @Param("keyword")String keyword, Pageable pageable);



    @Query("""
    select u
    from User u
    where u.status = :status
      and u.birthDate is not null
      and month(u.birthDate) = :month
    """)
    List<User> findBirthdayUsersByMonth(
            @Param("status") UserStatus status,
            @Param("month") int month
    );



    @Query("""
    select u
    from User u
    where u.status = :status
      and u.birthDate is not null
      and month(u.birthDate) = 2
      and day(u.birthDate) = 29
""")
    List<User> findBirthdayUsersIncludingLeap(@Param("status") UserStatus status);




}
