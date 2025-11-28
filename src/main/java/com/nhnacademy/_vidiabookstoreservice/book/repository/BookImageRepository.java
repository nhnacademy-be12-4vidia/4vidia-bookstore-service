package com.nhnacademy._vidiabookstoreservice.book.repository;

import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {

    boolean existsByBook_IdAndImageUrl(Long bookId, String imageUrl);

    @Query("SELECT bi.imageUrl FROM BookImage bi WHERE bi.imageUrl IN : urls")
    Set<String> findExistingUrls(@Param("urls") List<String> urls);
}
