package com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy;

import com.nhnacademy._vidiabookstoreservice.book.domain.DiscountPolicy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscountPolicyResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String kdcCode;
    private String discountPolicyName;
    private Integer discountRate;
    private LocalDate startDate;
    private LocalDate endDate;

    public static DiscountPolicyResponse fromEntity(DiscountPolicy policy) {
        return new DiscountPolicyResponse(
            policy.getId(),
            policy.getCategory() != null ? policy.getCategory().getId() : null,
            policy.getCategory() != null ? policy.getCategory().getCategoryName() : null,
            policy.getCategory() != null ? policy.getCategory().getKdcCode() : null,
            policy.getDiscountPolicyName(),
            policy.getDiscountRate(),
            policy.getStartDate(),
            policy.getEndDate()
        );
    }
}
