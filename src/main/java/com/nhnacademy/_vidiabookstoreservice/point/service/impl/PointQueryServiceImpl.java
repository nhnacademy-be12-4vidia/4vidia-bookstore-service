package com.nhnacademy._vidiabookstoreservice.point.service.impl;

import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointHistoryResponse;
import com.nhnacademy._vidiabookstoreservice.point.repository.PointDetailRepository;
import com.nhnacademy._vidiabookstoreservice.point.service.PointQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointQueryServiceImpl implements PointQueryService {

    private final PointDetailRepository pointDetailRepository;

    // 현재 잔여 포인트 조회
    @Override
    public int getRemainPoint(Long userId) {
        return pointDetailRepository.getRemainPoint(userId);
    }

    // 일장기간 내 만료 예정 포인트 금액 조회
    @Override
    @Transactional(readOnly = true)
    public int getExpiringPointWithinDays(Long userId, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusDays(days);
        return pointDetailRepository.getExpiringSoon(userId,now,limit);
    }

    // 포인트 내역 조회 ( 최신순)
    @Override
    @Transactional(readOnly = true)
    public Page<PointHistoryResponse> getHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return pointDetailRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(detail -> new PointHistoryResponse(
                        detail.getCreatedAt(),
                        detail.getPrice(),
                        detail.getReason().getTitle(),
                        detail.getOrderId(),
                        detail.getExpiredAt()
                ));
    }



}