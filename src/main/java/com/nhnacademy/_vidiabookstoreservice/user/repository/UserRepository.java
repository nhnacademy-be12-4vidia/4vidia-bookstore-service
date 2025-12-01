package com.nhnacademy._vidiabookstoreservice.user.repository;

import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    // 아이디 찾기
    Optional<User> findByNameAndBirthDateAndPhone(String name, LocalDate birthday, String phone);
    // 비밀번호 찾기
    Optional<User> findByEmailAndNameAndPhone(String email, String name, String phone);

    Optional<User> findByEmail(String email);
}
