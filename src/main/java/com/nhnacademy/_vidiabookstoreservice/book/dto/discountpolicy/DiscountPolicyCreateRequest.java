package com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy;

import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.domain.DiscountPolicy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DiscountPolicyCreateRequest {

    // nullable: 기본 할인은 카테고리id 없음
    private Long categoryId;

    @NotBlank(message = "정책명은 필수입니다.")
    private String discountPolicyName;

    @NotNull(message = "할인율은 필수입니다.")
    @Min(0)
    @Max(100)
    private Integer discountRate;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDate endDate;

    public DiscountPolicy toEntity(Category category) {
        return DiscountPolicy.builder()
            .category(category)
            .discountPolicyName(this.discountPolicyName)
            .discountRate(this.discountRate)
            .startDate(this.startDate)
            .endDate(this.endDate)
            .build();
    }
}
