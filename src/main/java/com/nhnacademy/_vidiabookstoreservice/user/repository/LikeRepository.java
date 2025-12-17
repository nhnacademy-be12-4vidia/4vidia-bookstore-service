package com.nhnacademy._vidiabookstoreservice.user.repository;

import com.nhnacademy._vidiabookstoreservice.user.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
/* N+1 문제...?
    //✅방법1: DTO 조회 (안전성 우선)
    //LikeResponse 같은 DTO를 JPQL에서 바로 조회하는 방법도 있음
    //예: SELECT new com.example.LikeResponse(l.likeId, l.book.title) FROM Like l WHERE l.user.userId = :userId
    //DTO 방식은 N+1 문제를 완전히 제거할 수 있음
    //장점: N+1 문제 없음
    //DB에서 바로 필요한 데이터만 조회
    //DTO 변환 불필요, 쿼리 단에서 바로 만들어짐
//    @Query("""
//        SELECT new com.nhnacademy._vidiabookstoreservice.user.dto.like.response.LikeResponse(
//            l.book.id, l.book.title, l.book.bookAuthors, l.book.priceStandard, l.book.priceSales, l.book.stockStatus, l.book.bookImageList
//        )
//        FROM Like l
//        WHERE l.user.userId = :userId
//    """)
//    List<LikeResponse> findAllByUserIdDto(@Param("userId") Long userId);

    //✅방법2: entity에 lazy만 붙이는 걸로는 쿼리수가 줄어들지 않음 (쿼리 성능 측정)
    // - 이렇게 하면 Like와 Book을 한 번의 쿼리로 가져올 수 있음
    // - 반복문에서 Book 접근해도 추가 쿼리 안 나감
//    @Query("SELECT l FROM Like l JOIN FETCH l.book WHERE l.user.userId = :userId")
//    List<Like> findAllByUserIdFetch(@Param("userId") Long userId);

    // => 3️⃣ 실무 추천
    //작은 데이터는 fetch join으로 충분
    //목록 조회 등 N이 커질 가능성이 있는 경우는 DTO 직조회가 안전하고 효율적

    일단 yml에 Bath Size 설정으로 성능 해결
*/
    List<Like> findAllByUser_UserId(Long userUserId); // n+1문제...?

    boolean existsByUser_UserIdAndBook_Id(Long userUserId, Long bookId);

    Optional<Like> findByUser_UserIdAndBook_Id(Long userUserId, Long bookId);

    List<Like> findAllByUser_UserIdAndBook_IdIn(Long userId, List<Long> bookIdList);
}
