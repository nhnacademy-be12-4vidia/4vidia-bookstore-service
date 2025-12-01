package com.nhnacademy._vidiabookstoreservice.book.repository;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    @Query("SELECT b FROM Book b JOIN FETCH b.bookAuthors ba JOIN FETCH ba.author WHERE b.id = :bookId")
    Optional<Book> findByIdWithAuthors(@Param("bookId") Long bookId);

    Page<Book> findByTitleContaining(String keyword, Pageable pageable);

    Page<Book> findByCategoryKdcCode(String categoryKdcCode, Pageable pageable);

    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Book> findByPublisherId(Long publisherId, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.bookAuthors ba WHERE ba.author.id = :authorId")
    Page<Book> findByAuthorId(@Param("authorId")Long authorId, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.category c WHERE c.path LIKE :pathPattern%")
    Page<Book> findAllByCategoryPath(@Param("pathPattern") String pathPattern, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithLock(Long bookId);

}
