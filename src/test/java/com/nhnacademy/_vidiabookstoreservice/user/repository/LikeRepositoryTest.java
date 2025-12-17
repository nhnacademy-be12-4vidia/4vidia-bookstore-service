package com.nhnacademy._vidiabookstoreservice.user.repository;


import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.domain.Publisher;

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
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("local")
class LikeRepositoryTest {
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private TestEntityManager entityManager;

    private User user;
    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp(){
        // 1. User 생성을 위한 Grade 준비
        Grade grade = Grade.builder()
                .gradeName(GradeName.WELCOME)
                .pointRate(1)
                .build();
        entityManager.persist(grade);


        // 2. User 생성
        user = User.builder()
                .email("liker@example.com")
                .password("pwd")
                .name("좋아요맨")
                .phone("010-1234-1234")
                .birthDate(LocalDate.of(2002,9,10))
                .grade(grade)
                .build();

        entityManager.persist(user);

        // 3. Book 생성을 위한 Publisher 준비
        Publisher publisher = Publisher.builder()
                .name("테스트출판사")
                .build();
        entityManager.persist(publisher);

        // category 생성 및 저장
        Category category = Category.builder()
                .kdcCode("z20")
                .categoryName("테스트 카테고리")
                .path("001")
                .depth(1)
                .parentCategory(null)
                .build();
        entityManager.persist(category);

        // 4. Book 생성 맟 조정
        book1 = Book.builder()
                .isbn("1111111111111")
                .title("Test Book 1")
                .description("Desc 1")
                .publisher(publisher)      // 연관관계 설정
                .category(category)        // 연관관계 설정
                .publishedDate(LocalDate.now())
                .priceStandard(10000)
                .priceSales(9000)
                .stock(10)
                .packagingAvailable(true)
                .build();
        entityManager.persist(book1);

        book2 = Book.builder()
                .isbn("2222222222222")
                .title("Test Book 2")
                .description("Desc 2")
                .publisher(publisher)
                .category(category)
                .publishedDate(LocalDate.now())
                .priceStandard(20000)
                .priceSales(18000)
                .stock(5)
                .packagingAvailable(true)
                .build();
        entityManager.persist(book2);

        // Like 데이터 저장
        Like like1 = Like.builder().user(user).book(book1).build();
        Like like2 = Like.builder().user(user).book(book2).build();

        likeRepository.save(like1);
        likeRepository.save(like2);

        // 영속성 컨텍스트 초기화 (실제 DB 쿼리 발생 확인용)
        entityManager.flush();
        entityManager.clear();

    }

    @Test
    @DisplayName("사용자 ID로 모든 좋아요 목록 조회")
    void findAllByUser_UserId(){
        // when
        List<Like> likes = likeRepository.findAllByUser_UserId(user.getUserId());

        //then
        assertThat(likes).hasSize(2);
        assertThat(likes).extracting(like->like.getBook().getTitle())
                .containsExactlyInAnyOrder("Test Book 1",  "Test Book 2");

    }

    @Test
    @DisplayName("특정 사용자가 특정 책을 좋아요 했는지 확인 - True")
    void existsByUser_USerIdAndBook_Id_True(){
        boolean exists = likeRepository.existsByUser_UserIdAndBook_Id(user.getUserId(), book1.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("특정 사용자가 특정 책을 좋아요 했는지 호가인 - False")
    void existsByUser_UserIdAndBook_Id_False(){
        boolean exists = likeRepository.existsByUser_UserIdAndBook_Id(user.getUserId(), 999999L);
        assertThat(exists).isFalse();
    }




}