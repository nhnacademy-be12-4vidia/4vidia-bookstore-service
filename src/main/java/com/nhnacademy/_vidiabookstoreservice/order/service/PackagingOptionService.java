package com.nhnacademy._vidiabookstoreservice.order.service;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.PackagingOptionRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.PackagingOptionResponse;

import java.util.List;

public interface PackagingOptionService {

    void savePackagingOption(PackagingOptionRequest packagingOptionRequest);

    List<PackagingOptionResponse> getPackagingOptions();

    PackagingOptionResponse getByPackagingOptionId(Long packagingOptionId);

}
