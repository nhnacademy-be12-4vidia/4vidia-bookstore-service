package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.domain.Packaging;
import com.nhnacademy._vidiabookstoreservice.order.repository.PackagingRepository;
import com.nhnacademy._vidiabookstoreservice.order.service.PackagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PackagingServiceImpl implements PackagingService {

    private final PackagingRepository packagingRepository;

    public Packaging addPackaging(Packaging packaging) {
        return packagingRepository.save(packaging);
    }
}
