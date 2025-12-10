package com.nhnacademy._vidiabookstoreservice.point.service.impl;

import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
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
        LocalDate now = LocalDate.now();
        LocalDate limit = now.plusDays(days);
        return pointDetailRepository.getExpiringSoon(userId,now,limit);
    }

    // 포인트 내역 조회 ( 최신순)
    @Override
    @Transactional(readOnly = true)
//    public Page<PointHistoryResponse> getHistory(Long userId,String category, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//
//        return pointDetailRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
//                .map(detail -> new PointHistoryResponse(
//                        detail.getCreatedAt(),
//                        detail.getPrice(),
//                        detail.getReason().getTitle(),
//                        detail.getPointPolicy() != null ? detail.getPointPolicy().getPointName() : null,
//                        detail.getExpiredDate()
//                ));
//    }
    public Page<PointHistoryResponse> getHistory(Long userId, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        String cat = (category == null) ? "ALL" : category.toUpperCase();

        // 🔥 카테고리별로 다른 쿼리 사용
        Page<PointDetail> details;

        switch (cat) {
            case "EARN":   // 적립 내역: price > 0
                details = pointDetailRepository
                        .findByUserIdAndPriceGreaterThanOrderByCreatedAtDesc(userId, 0, pageable);
                break;

            case "USE":    // 사용 내역: price < 0
                details = pointDetailRepository
                        .findByUserIdAndPriceLessThanOrderByCreatedAtDesc(userId, 0, pageable);
                break;

            case "ALL":
            default:       // 전체 내역
                details = pointDetailRepository
                        .findByUserIdOrderByCreatedAtDesc(userId, pageable);
                break;
        }

        // 엔티티 → DTO 매핑은 그대로 사용
        return details.map(detail -> new PointHistoryResponse(
                detail.getCreatedAt(),
                detail.getPrice(),
                detail.getReason().getTitle(),
                detail.getPointPolicy() != null ? detail.getPointPolicy().getPointName() : null,
                detail.getExpiredDate()
        ));
    }

}