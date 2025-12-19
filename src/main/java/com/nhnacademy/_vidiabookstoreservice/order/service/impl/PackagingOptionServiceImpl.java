package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.domain.PackagingOption;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.PackagingOptionRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.PackagingOptionResponse;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.PackagingOptionNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.PackagingOptionRepository;
import com.nhnacademy._vidiabookstoreservice.order.service.PackagingOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PackagingOptionServiceImpl implements PackagingOptionService {
    private final PackagingOptionRepository packagingOptionRepository;

    public PackagingOption savePackagingOption(PackagingOptionRequest packagingOptionRequest) {
        PackagingOption packagingOption = PackagingOption.builder()
                .name(packagingOptionRequest.name())
                .price(packagingOptionRequest.price())
                .build();

        return packagingOptionRepository.save(packagingOption);
    }

    @Transactional(readOnly = true)
    public List<PackagingOptionResponse> getPackagingOptions() {
        List<PackagingOption> packagingOptions = packagingOptionRepository.findAll();

        return packagingOptions.stream().map(PackagingOptionResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PackagingOption getByPackagingOptionId(Long packagingOptionId) {

        return packagingOptionRepository.findByPackagingOptionId(packagingOptionId)
                .orElseThrow(() -> new PackagingOptionNotFoundException(packagingOptionId));
    }

}
