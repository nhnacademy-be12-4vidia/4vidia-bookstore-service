package com.nhnacademy._vidiabookstoreservice.order.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class BestSellerScheduler {

    @Qualifier("bestsellerRedisTemplate")
    private final StringRedisTemplate bestsellerRedisTemplate;

    private static final String SOURCE_KEY = "bestseller"; // 누적 판매량 (계속 쌓이게)
    private static final String VIEW_KEY = "top10"; // 조회용 캐시 (사용자에게 보여줌)
    private static final String BACKUP_KEY = "bestseller:prev"; // 데이터 부족 시 채워넣을 백업용

    /**
     * 매 시간 베스트셀러 랭킹 갱신
     * 1. SOURCE_KEY(ZSet) -> Top 10 추출
     * 2. VIEW_KEY(List)에 덮어쓰기
     * 3. SOURCE_KEY -> BACKUP_KEY로 백업 후 초기화
     */
    @Scheduled(cron = "0 0/5 * * * *") // 테스트용 5분마다 스케줄링
//    @Scheduled(cron = "0 0 * * * *")
    public void updateBestsellerRanking() {
        log.info("[Scheduler] 베스트셀러 일일 집계 및 초기화 시작");

        ZSetOperations<String, String> zSetOps = bestsellerRedisTemplate.opsForZSet();
        ListOperations<String, String> listOps = bestsellerRedisTemplate.opsForList();
        
        // 1. 오늘의 Top 10 조회 (점수 높은 순)
        // reverseRangeWithScores를 쓰면 LinkedHashSet으로 반환되어 순서가 유지됩니다.
        Set<ZSetOperations.TypedTuple<String>> top10Tuples =
                zSetOps.reverseRangeWithScores(SOURCE_KEY, 0, 9);

        // Tuple에서 BookId(Value)만 추출하여 리스트로 변환
        List<String> newRankingList = new ArrayList<>();
        if (top10Tuples != null) {
            newRankingList = top10Tuples.stream()
                    .map(ZSetOperations.TypedTuple::getValue)
                    .collect(Collectors.toList());
        }

        // 2. (선택사항) 데이터가 10개 미만일 때 어제 데이터(백업)에서 채우기 로직
        if (newRankingList.size() < 10) {
            fillInsufficientData(newRankingList);
        }

        // 3. 결과 저장 (List 구조 사용 -> 순서 100% 보장)
        if (!newRankingList.isEmpty()) {
            bestsellerRedisTemplate.delete(VIEW_KEY); // 기존 랭킹 삭제
            listOps.rightPushAll(VIEW_KEY, newRankingList); // 새 랭킹 저장
            log.info("새로운 Top 10 랭킹 반영 완료: {}", newRankingList);
        } else {
            log.warn("판매 데이터가 0건입니다.");
        }

        // 4. 원본 데이터 백업 및 초기화 (bestseller -> bestseller:prev)
        if (Boolean.TRUE.equals(bestsellerRedisTemplate.hasKey(SOURCE_KEY))) {
            bestsellerRedisTemplate.rename(SOURCE_KEY, BACKUP_KEY);
        }


        log.info("[Scheduler] 일일 집계 완료. 데이터 초기화 됨.");
    }

    // 부족한 개수만큼 백업 키(어제 랭킹 등)에서 가져와 채우는 메서드
    private void fillInsufficientData(List<String> currentList) {
        int needCount = 10 - currentList.size();
        ZSetOperations<String, String> zSetOps = bestsellerRedisTemplate.opsForZSet();

        // 백업 키에서 상위 N개 가져오기
        Set<String> backupIds = zSetOps.reverseRange(BACKUP_KEY, 0, 19); // 넉넉하게 20개 가져와서 중복 제거

        if (backupIds != null) {
            for (String id : backupIds) {
                if (needCount <= 0) break;
                if (!currentList.contains(id)) { // 이미 오늘 팔린 책이 아니라면 추가
                    currentList.add(id);
                    needCount--;
                }
            }
        }
        log.info("부족한 데이터를 백업에서 {}개 채웠습니다.", 10 - currentList.size() - needCount);
    }
}
