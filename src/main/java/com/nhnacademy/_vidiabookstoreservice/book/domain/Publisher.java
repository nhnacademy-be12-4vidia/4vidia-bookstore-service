package com.nhnacademy._vidiabookstoreservice.book.domain;

import com.nhnacademy._vidiabookstoreservice.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "publisher")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Publisher extends BaseEntity {
    @Id
    @Column(name = "publisher_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "publisher_name", nullable = false, unique = true, length = 255)
    String name;

    @Builder
    public Publisher(String name) {
        this.name = name;
    }

}
