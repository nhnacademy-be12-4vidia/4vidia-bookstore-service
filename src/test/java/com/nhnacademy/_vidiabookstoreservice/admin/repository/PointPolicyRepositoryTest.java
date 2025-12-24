package com.nhnacademy._vidiabookstoreservice.admin.repository;

import com.nhnacademy._vidiabookstoreservice.admin.domain.PointPolicy;
import com.nhnacademy._vidiabookstoreservice.book.config.QueryDslConfig;
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
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("local")
@AutoConfigureTestDatabase(replace =  AutoConfigureTestDatabase.Replace.NONE)
@Import(QueryDslConfig.class)
class PointPolicyRepositoryTest {
    @Autowired
    private PointPolicyRepository pointPolicyRepository;


    @Test
    @DisplayName("DB에 이미 존재하는 정책을 ID로 조회할 수 있다")
    void findByPointPolicyId_existingData() {
        // when
        Optional<PointPolicy> result = pointPolicyRepository.findByPointPolicyId(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPointPolicyId()).isEqualTo(1L);
        assertThat(result.get().getPointName()).isEqualTo("회원가입");
        assertThat(result.get().getPrice()).isEqualTo(5000);
    }

    @Test
    @DisplayName("존재하지 않는 정책 ID 조회 시 empty")
    void findByPointPolicyId_notFound(){
        assertThat(pointPolicyRepository.findByPointPolicyId(999L)).isEmpty();
    }


}