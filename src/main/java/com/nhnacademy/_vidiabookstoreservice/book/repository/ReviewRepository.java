package com.nhnacademy._vidiabookstoreservice.book.repository;

import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.book.id = :bookId")
    Page<Review> findByBookId(@Param("bookId") Long bookId, Pageable pageable);

    @Query("""
        SELECT r.orderItem.orderItemId FROM Review r WHERE r.orderItem.orderItemId in :orderItemIdList
        """)
    List<Long> findReviewedOrderItemIdList(@Param("orderItemIdList") List<Long> orderItemIdList);

}
