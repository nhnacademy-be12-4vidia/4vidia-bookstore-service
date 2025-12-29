package com.nhnacademy._vidiabookstoreservice.user.repository;


import com.nhnacademy._vidiabookstoreservice.book.config.QueryDslConfig;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.domain.Publisher;

import com.nhnacademy._vidiabookstoreservice.book.domain.enums.StockStatus;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.Like;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class LikeRepositoryTest {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;
    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        // 1. Grade 저장
        Grade grade = Grade.builder()
                .gradeName(GradeName.WELCOME)
                .pointRate(1)
                .build();
        entityManager.persist(grade);

        // 2. User 저장
        user = User.builder()
                .email("liker@example.com")
                .password("pwd")
                .name("좋아요맨")
                .phone("010-1234-1234")
                .birthDate(LocalDate.of(2002, 9, 10))
                .grade(grade)
                .build();
        entityManager.persist(user);

        // 3. Publisher 저장
        Publisher publisher = Publisher.builder().name("테스트출판사").build();
        entityManager.persist(publisher);

        // 4. Category 저장
        Category category = Category.builder()
                .kdcCode("z20")
                .categoryName("테스트 카테고리")
                .path("001")
                .depth(1)
                .build();
        entityManager.persist(category);

        // 5. Book 저장
        book1 = Book.builder()
                .isbn("1111111111111")
                .title("Test Book 1")
                .publisher(publisher)
                .category(category)
                .priceStandard(10000)
                .priceSales(9000)
                .stock(10)
                .stockStatus(StockStatus.IN_STOCK)
                .packagingAvailable(true)
                .build();
        entityManager.persist(book1);

        book2 = Book.builder()
                .isbn("2222222222222")
                .title("Test Book 2")
                .publisher(publisher)
                .category(category)
                .priceStandard(20000)
                .priceSales(18000)
                .stock(5)
                .stockStatus(StockStatus.IN_STOCK)
                .packagingAvailable(true)
                .build();
        entityManager.persist(book2);

        // 6. Like 저장
        Like like1 = Like.builder().user(user).book(book1).build();
        Like like2 = Like.builder().user(user).book(book2).build();
        entityManager.persist(like1);
        entityManager.persist(like2);

        entityManager.flush();
        // 주의: 여기서 clear()를 하면 user.getUserId() 등이 null이 될 수 있으므로 생략하거나
        // ID를 별도 변수에 담아야 합니다. 여기서는 객체를 직접 사용합니다.
    }

    @Test
    @DisplayName("1. findByUser_UserId (Pageable): 페이징 처리된 좋아요 목록 조회")
    void findByUser_UserId() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Like> page = likeRepository.findByUser_UserId(user.getUserId(), pageRequest);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(l -> l.getBook().getTitle())
                .containsExactlyInAnyOrder("Test Book 1", "Test Book 2");
    }

    @Test
    @DisplayName("2. findAllByUser_UserId: 모든 좋아요 리스트 조회")
    void findAllByUser_UserId() {
        List<Like> likes = likeRepository.findAllByUser_UserId(user.getUserId());

        assertThat(likes).hasSize(2);
    }

    @Test
    @DisplayName("3. existsByUser_UserIdAndBook_Id: 존재 여부 확인 (True/False)")
    void existsByUser_UserIdAndBook_Id() {
        // True Case
        boolean exists = likeRepository.existsByUser_UserIdAndBook_Id(user.getUserId(), book1.getId());
        assertThat(exists).isTrue();

        // False Case
        boolean notExists = likeRepository.existsByUser_UserIdAndBook_Id(user.getUserId(), 999L);
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("4. findByUser_UserIdAndBook_Id: 단건 조회 (Optional)")
    void findByUser_UserIdAndBook_Id() {
        Optional<Like> foundLike = likeRepository.findByUser_UserIdAndBook_Id(user.getUserId(), book1.getId());

        assertThat(foundLike).isPresent();
        assertThat(foundLike.get().getBook().getId()).isEqualTo(book1.getId());
    }

    @Test
    @DisplayName("5. findAllByUser_UserIdAndBook_IdIn: ID 리스트로 다중 조회")
    void findAllByUser_UserIdAndBook_IdIn() {
        List<Long> bookIds = List.of(book1.getId(), book2.getId());
        List<Like> likes = likeRepository.findAllByUser_UserIdAndBook_IdIn(user.getUserId(), bookIds);

        assertThat(likes).hasSize(2);
        assertThat(likes).extracting(l -> l.getBook().getId())
                .contains(book1.getId(), book2.getId());
    }
}