package com.nhnacademy._vidiabookstoreservice.user.domain;


import com.nhnacademy._vidiabookstoreservice.user.domain.converters.GradeNameConverter;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grade")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gradeId;

    @Column(columnDefinition = "TINYINT", nullable = false)
    @Convert(converter = GradeNameConverter.class)
    private GradeName gradeName;

    @Column(nullable = false)
    private Integer pointRate;


    @Builder
    public Grade(GradeName gradeName, Integer pointRate) {
        this.gradeName = gradeName;
        this.pointRate = pointRate;
    }

}
