package com.nhnacademy._vidiabookstoreservice.order.dto.packaging.response;

import com.nhnacademy._vidiabookstoreservice.order.domain.Packaging;
import com.nhnacademy._vidiabookstoreservice.order.domain.PackagingOption;

public record PackagingResponse(
        Long packagingOptionId,
        String name,
        int price
) {
    public static PackagingResponse from(Packaging packaging) {
        PackagingOption option = packaging.getPackagingOption();

        return new PackagingResponse(
                option.getPackagingOptionId(),
                option.getName(),
                option.getPrice()
        );
    }
}