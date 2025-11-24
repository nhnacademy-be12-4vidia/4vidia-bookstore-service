package com.nhnacademy._vidiabookstoreservice.book.domain;

import com.nhnacademy._vidiabookstoreservice.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "category", indexes = {
    @Index(name = "idx_category_parent_category_id", columnList = "parent_category_id"),
    @Index(name = "idx_category_name", columnList = "category_name"),
    @Index(name = "idx_category_path", columnList = "path"),
    @Index(name = "idx_category_depth", columnList = "depth")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    @Setter
    private Category parentCategory;

    @Column(name = "kdc_code", nullable = false, unique = true, length = 3)
    @Setter
    private String kdcCode;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "path", nullable = false, length = 20)
    @Setter
    private String path;

    @Column(name = "depth", nullable = false, columnDefinition = "TINYINT")
    @Setter
    private Integer depth;


    @Builder
    public Category(Category parentCategory, String kdcCode, String categoryName, String path, Integer depth) {
        this.parentCategory = parentCategory;
        this.kdcCode = kdcCode;
        this.categoryName = categoryName;
        this.path = path;
        this.depth = depth;
    }

}
