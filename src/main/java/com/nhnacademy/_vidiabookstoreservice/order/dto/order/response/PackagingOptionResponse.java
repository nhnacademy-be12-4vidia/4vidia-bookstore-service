package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;

import com.nhnacademy._vidiabookstoreservice.order.domain.PackagingOption;

public record PackagingOptionResponse(
        long packagingOptionId,
        String name,
        int price
) {
    public static PackagingOptionResponse from(PackagingOption packagingOption) {
        return new PackagingOptionResponse(
                packagingOption.getPackagingOptionId(),
                packagingOption.getName(),
                packagingOption.getPrice()
        );
    }
}
