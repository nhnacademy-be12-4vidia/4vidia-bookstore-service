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


    // 관리자 페이지 리뷰조회
    @Query("""
       select r
       from Review r
       join r.book b
       join r.user u
       where (:keyword is null
              or lower(b.title) like lower(concat('%', :keyword, '%'))
              or lower(r.content) like lower(concat('%', :keyword, '%'))
              or lower(u.email) like lower(concat('%', :keyword, '%')))
         and (:rating is null or r.rating = :rating)
       """)
    Page<Review> searchAdminReviews(@Param("keyword") String keyword,
                                    @Param("rating") Integer rating,
                                    Pageable pageable);

}
