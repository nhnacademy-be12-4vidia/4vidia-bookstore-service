package com.nhnacademy._vidiabookstoreservice.book.repository;

import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
        
        List<Category> findAllByOrderByKdcCodeAsc();

        Optional<Category> findByKdcCode(String kdcCode);

        boolean existsByKdcCode(String kdcCode);

        boolean existsByParentCategoryId(Long parentId);

        @Query("SELECT c.path FROM Category c WHERE c.id = :categoryId")
        String findPathByCategoryId(@Param("categoryId") Long categoryId);
}
