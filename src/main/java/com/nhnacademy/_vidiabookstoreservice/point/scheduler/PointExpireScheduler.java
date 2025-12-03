package com.nhnacademy._vidiabookstoreservice.point.scheduler;


import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import com.nhnacademy._vidiabookstoreservice.point.repository.PointDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointExpireScheduler {
    private final PointDetailRepository pointDetailRepository;

    /**
     * 매일 새벽 03:00에 유효기간이 지난 적립 포인트 자동 소멸
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void expirePoints(){
        LocalDateTime now = LocalDateTime.now();
        List<PointDetail> expireList =
                pointDetailRepository.findExpiredPoints(now);

        if(expireList.isEmpty()){
            log.info("[PointExpireScheduler] No points to expire.");
            return;
        }

        expireList.forEach(point ->{
            log.info(
                    "[PointExpireScheduler] Expired point → userId={}, priceBefore={}, expiredAt={}",
                    point.getUserId(),
                    point.getPrice(),
                    point.getExpiredAt()
            );
            point.expire();
        });
        log.info("[PointExpireScheduler] Total expired points count = {}", expireList.size());
    }
}
