package com.nhnacademy._vidiabookstoreservice.book.repository;

import com.nhnacademy._vidiabookstoreservice.book.domain.BookReviewSummary;
import feign.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookReviewSummaryRepository extends JpaRepository<BookReviewSummary, Long> {

    @Query("""
                    SELECT s FROM BookReviewSummary s 
                    WHERE s.dirty = true
                    AND s.dirtyCount >= :threshold
                    AND s.status = 0
                    ORDER BY s.dirtyCount DESC 
            """)
    List<BookReviewSummary> findTargets(@Param("threshold") int threshold, Pageable pageable);

    @Modifying
    @Query("""
                    UPDATE BookReviewSummary s
                    SET s.status = 1
                    WHERE s.bookId = :bookId
                    AND s.status = 0
            """)
    int tryMarkRunning(@Param("bookId") Long bookId);
}
