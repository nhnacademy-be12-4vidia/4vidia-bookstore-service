package com.nhnacademy._vidiabookstoreservice.user.repository;

import com.nhnacademy._vidiabookstoreservice.user.domain.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Page<Like> findByUser_UserId(Long userId, Pageable pageable);
    List<Like> findAllByUser_UserId(Long userUserId);

    boolean existsByUser_UserIdAndBook_Id(Long userUserId, Long bookId);

    Optional<Like> findByUser_UserIdAndBook_Id(Long userUserId, Long bookId);

    List<Like> findAllByUser_UserIdAndBook_IdIn(Long userId, List<Long> bookIdList);
}
