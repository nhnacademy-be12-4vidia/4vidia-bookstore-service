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
    @Column(name = "grade_id")
    private Long gradeId;

    @Column(name = "grade_name", columnDefinition = "TINYINT", nullable = false)
    @Convert(converter = GradeNameConverter.class)
    private GradeName gradeName;

    @Column(name = "point_rate", nullable = false)
    private Integer pointRate;




    @Builder
    public Grade(GradeName gradeName, Integer pointRate) {
        this.gradeName = gradeName;
        this.pointRate = pointRate;
    }
}
