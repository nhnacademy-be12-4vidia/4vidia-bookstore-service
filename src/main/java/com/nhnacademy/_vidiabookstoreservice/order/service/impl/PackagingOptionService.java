package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.domain.PackagingOption;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.PackagingOptionRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.PackagingOptionResponse;
import com.nhnacademy._vidiabookstoreservice.order.repository.PackagingOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PackagingOptionService {
    private final PackagingOptionRepository packagingOptionRepository;

    public void savePackagingOption(PackagingOptionRequest packagingOptionRequest) {
        PackagingOption packagingOption = PackagingOption.builder()
                .name(packagingOptionRequest.name())
                .price(packagingOptionRequest.price())
                .build();

        packagingOptionRepository.save(packagingOption);
    }

    public List<PackagingOptionResponse> getPackagingOptions() {
        List<PackagingOption> packagingOptions = packagingOptionRepository.findAll();

        List<PackagingOptionResponse> packagingOptionResponseList = new ArrayList<>();

        for (PackagingOption packagingOption : packagingOptions) {
            packagingOptionResponseList.add(new PackagingOptionResponse(
                    packagingOption.getPackagingOptionId(),
                    packagingOption.getName(),
                    packagingOption.getPrice())
            );
        }
        return packagingOptionResponseList;
    }

}
