package com.nhnacademy._vidiabookstoreservice.book.domain;

import com.nhnacademy._vidiabookstoreservice.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tag")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Tag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(name = "tag_name", nullable = false, unique = true, length = 255)
    private String name;

    @Builder
    public Tag(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("태그 이름은 필수입니다.");
        }
        this.name = name.trim();
    }
}
