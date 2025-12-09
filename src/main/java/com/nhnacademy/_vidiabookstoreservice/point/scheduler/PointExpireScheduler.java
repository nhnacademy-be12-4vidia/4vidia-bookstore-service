package com.nhnacademy._vidiabookstoreservice.point.scheduler;


import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import com.nhnacademy._vidiabookstoreservice.point.repository.PointDetailRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointExpireScheduler {
    private final PointDetailRepository pointDetailRepository;
    private final UserRepository userRepository;

    /**
     * 매일 새벽 00:00에 유효기간이 지난 적립 포인트 자동 소멸
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void expirePoints(){
        LocalDate now = LocalDate.now();
        List<PointDetail> expireList =
                pointDetailRepository.findExpiredPoints(now);

        if(expireList.isEmpty()){
            log.info("[포인트 소멸 스케줄러] 소멸 시킬 포인트가 존재하지 않습니다.");
            return;
        }

        expireList.forEach(point ->{
            log.info(
                    "[포인트 소멸 스케줄러] 소멸 포인트 → userId={}, priceBefore={}, expiredAt={}",
                    point.getUserId(),
                    point.getPrice(),
                    point.getExpiredDate()
            );

            pointDetailRepository.save(PointDetail.expired(point.getUserId())); // 소멸 기록
            point.setRemainingPrice(0); // 기존 적립 내역의 잔여 포인트 0으로 수정

            userRepository.findById(point.getUserId()).ifPresent(user -> {
                user.subtractPoint(point.getPrice());
            });

        });
        log.info("[포인트 소멸 케줄러] 전체 소멸 포인트 = {}", expireList.size());
    }
}
