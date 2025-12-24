package com.nhnacademy._vidiabookstoreservice.point.service;

import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointHistoryResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface PointQueryService {
    int getExpiringPointWithinDays(Long userId, int days);

    Page<PointHistoryResponse> getHistory(Long userId, String category,
                                          LocalDate from, LocalDate to,
                                          int page, int size);
}