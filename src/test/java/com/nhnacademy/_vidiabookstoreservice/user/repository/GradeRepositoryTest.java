package com.nhnacademy._vidiabookstoreservice.user.repository;

import com.nhnacademy._vidiabookstoreservice.book.config.QueryDslConfig;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class GradeRepositoryTest {
    @Autowired
    private GradeRepository gradeRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("등급 저장 및 ID로 조회")
    void saveAndFindById(){
        //given
        Grade grade = Grade.builder()
                .gradeName(GradeName.GOLD)
                .pointRate(5)
                .build();

        //when
        Grade savedGrade = gradeRepository.save(grade);

        // 영속성 컨텍스트 초기화(실제 DB 조회 확인용)
        entityManager.flush();
        entityManager.clear();

        //then
        Optional<Grade> foundGrade = gradeRepository.findById(savedGrade.getGradeId());

        assertThat(foundGrade).isPresent();
        assertThat(foundGrade.get().getGradeName()).isEqualTo(GradeName.GOLD);
        assertThat(foundGrade.get().getPointRate()).isEqualTo(5);

    }

    @Test
    @DisplayName("등급 이름(GradeName)으로 조회")
    void findByGradeName(){
        Grade royalGrade = Grade.builder()
                .gradeName(GradeName.ROYAL)
                        .pointRate(3)
                                .build();
        entityManager.persist(royalGrade);

        entityManager.flush();
        entityManager.clear();

        Grade result = gradeRepository.findByGradeName(GradeName.ROYAL);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getGradeName()).isEqualTo(GradeName.ROYAL);
        assertThat(result.getPointRate()).isEqualTo(3);

    }


}