package com.nhnacademy._vidiabookstoreservice.book.repository;

import com.nhnacademy._vidiabookstoreservice.book.domain.ReviewImage;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    boolean existsByReview_IdAndImageUrl(Long reviewId, String imageUrl);

    @Query("SELECT ri FROM ReviewImage ri WHERE ri.imageUrl IN :urls")
    Set<String> findExistingUrls(@Param("urls") List<String> urls);
}
