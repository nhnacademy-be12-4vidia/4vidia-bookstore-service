package com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DiscountPolicyUpdateRequest {

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
}
