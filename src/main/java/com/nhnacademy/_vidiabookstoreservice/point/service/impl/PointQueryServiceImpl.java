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

    // 일정기간 내 만료 예정 포인트 금액 조회
    @Override
    public int getExpiringPointWithinDays(Long userId, int days) {
        LocalDate now = LocalDate.now();
        LocalDate limit = now.plusDays(days);
        return pointDetailRepository.getExpiringSoon(userId,now,limit);
    }

    @Override
    public Page<PointHistoryResponse> getHistory(Long userId, String category,
                                                 LocalDate from, LocalDate to,
                                                 int page, int size) {

        // Pageable에서 정렬 담당 (repo에 OrderBy 없어도 최신순 유지됨)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        String cat = (category == null) ? "ALL" : category.toUpperCase();

        //  null 방어 + 기본값(최근 3개월)
        LocalDate end = (to != null) ? to : LocalDate.now();
        LocalDate startDate = (from != null) ? from : end.minusMonths(3);

        //  from > to 방어
        if (startDate.isAfter(end)) {
            LocalDate tmp = startDate;
            startDate = end;
            end = tmp;
        }

        // to 날짜 포함 위해 endExclusive 사용
        var start = startDate.atStartOfDay();
        var endExclusive = end.plusDays(1).atStartOfDay();

        Page<PointDetail> details;

        switch (cat) {
            case "EARN":
                details = pointDetailRepository
                        .findByUserIdAndCreatedAtBetweenAndPriceGreaterThan(
                                userId, start, endExclusive, 0, pageable
                        );
                break;

            case "USE":
                details = pointDetailRepository
                        .findByUserIdAndCreatedAtBetweenAndPriceLessThan(
                                userId, start, endExclusive, 0, pageable
                        );
                break;

            case "ALL":
            default:
                details = pointDetailRepository
                        .findByUserIdAndCreatedAtBetween(
                                userId, start, endExclusive, pageable
                        );
                break;
        }

        return details.map(detail -> new PointHistoryResponse(
                detail.getCreatedAt(),
                detail.getPrice(),
                detail.getReason().getTitle(),
                detail.getPointPolicy() != null ? detail.getPointPolicy().getPointName() : null,
                detail.getExpiredDate()
        ));
    }



}