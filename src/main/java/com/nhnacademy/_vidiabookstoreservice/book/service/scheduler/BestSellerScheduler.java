package com.nhnacademy._vidiabookstoreservice.book.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class BestSellerScheduler {
    private final StringRedisTemplate bestsellerRedisTemplate;

    private static final String KEY_DAILY_SALES_STATS = "stats:bestseller:daily";
    private static final String KEY_BESTSELLER_VIEW_CACHE = "view:bestseller:top10";
    private static final String KEY_YESTERDAY_BACKUP = "backup:bestseller:yesterday";

    /**
     * 1시간마다 랭킹 갱신
     */
    @Scheduled(cron = "0 0 * * * *")
    public void updateBestsellerRanking() {
        log.info("========== [Scheduler] 🥇 베스트셀러 랭킹 집계 시작 ==========");

        ZSetOperations<String, String> zSetOps = bestsellerRedisTemplate.opsForZSet();
        ListOperations<String, String> listOps = bestsellerRedisTemplate.opsForList();

        Set<String> finalRankingSet = new LinkedHashSet<>(); // 최종 ID 저장용 (중복 제거, 순서 유지)
        List<String> logMessages = new ArrayList<>(); // 로그 출력용 메시지 저장

        // 1. 오늘의 Top 10 조회 (Score 포함)
        // reverseRangeWithScores를 사용하면 값(Value)과 점수(Score)를 같이 줍니다.
        Set<TypedTuple<String>> todayTuples = zSetOps.reverseRangeWithScores(KEY_DAILY_SALES_STATS, 0, 9);
        if (todayTuples != null) {
            for (TypedTuple<String> tuple : todayTuples) {
                String bookId = tuple.getValue();
                Double score = tuple.getScore(); // 판매량
                int salesCount = (score != null) ? score.intValue() : 0;

                finalRankingSet.add(bookId);
                // 로그 메시지 포맷: "ID (판매량) *"
                logMessages.add(String.format("📖 도서ID: %s (%d권) *", bookId, salesCount));
            }
        }
        log.info(">> 오늘 판매 데이터: {}건 반영됨", finalRankingSet.size());


        // 2. 데이터가 10개 미만일 때 어제 데이터(백업)에서 채우기 로직
        if (finalRankingSet.size() < 10) {
            int needed = 10 - finalRankingSet.size();
            log.info(">> 데이터 부족 (현재 {}개). 백업 데이터 보충 시도...", finalRankingSet.size());

            // 백업 데이터 조회 (Score 포함)
            Set<TypedTuple<String>> backupTuples = zSetOps.reverseRangeWithScores(KEY_YESTERDAY_BACKUP, 0, 19);

            if (backupTuples != null) {
                for (TypedTuple<String> tuple : backupTuples) {
                    if (finalRankingSet.size() >= 10) break;

                    String bookId = tuple.getValue();
                    // add가 true를 반환하면 -> Set에 없던 새로운 값이므로 추가 성공 (즉, 오늘 판매된 책이 아님)
                    if (finalRankingSet.add(bookId)) {
                        Double score = tuple.getScore();
                        int salesCount = (score != null) ? score.intValue() : 0;

                        // 백업 데이터는 '*' 표시 없음
                        logMessages.add(String.format("📖 도서ID: %s (%d권)", bookId, salesCount));
                    }
                }
            }
        }

        // 3. 결과 Redis 저장 (Atomic Rename)
        List<String> finalRankingList = new ArrayList<>(finalRankingSet);
        if (!finalRankingList.isEmpty()) {
            String tempKey = "temp:bestseller:update:" + UUID.randomUUID();

            listOps.rightPushAll(tempKey, finalRankingList);
            bestsellerRedisTemplate.expire(tempKey, 60, java.util.concurrent.TimeUnit.SECONDS);
            bestsellerRedisTemplate.rename(tempKey, KEY_BESTSELLER_VIEW_CACHE);

            log.info(">> 최종 랭킹 반영 완료 (총 {}권)", finalRankingList.size());

            // 요청하신 상세 로그 출력
            for (int i = 0; i < logMessages.size(); i++) {
                log.info("{}위 - {}", i + 1, logMessages.get(i));
            }

        } else {
            // 오늘 데이터도 없고 백업도 없어서 리스트가 비었을 때 (삭제 대신 유지하도록 정책 변경 시 이 부분 수정 가능)
            log.warn(">> 판매 데이터 0건. 베스트셀러 리스트 갱신 없음 (기존 데이터 유지 또는 삭제)");
        }

        log.info("========================================================");
    }

    /**
     * 매일 자정 실행
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void dailyReset() {
        log.info("========== [Scheduler] 일일 데이터 초기화 (오늘 -> 어제) ==========");

        if (Boolean.TRUE.equals(bestsellerRedisTemplate.hasKey(KEY_DAILY_SALES_STATS))) {
            // Rename(덮어쓰기) 사용
            // 오늘 쌓인 데이터를 백업 키로 이름만 바꿈 (기존 백업 데이터는 사라짐 -> 누적 방지)
            bestsellerRedisTemplate.rename(KEY_DAILY_SALES_STATS, KEY_YESTERDAY_BACKUP);
            bestsellerRedisTemplate.expire(KEY_YESTERDAY_BACKUP, 3, java.util.concurrent.TimeUnit.DAYS); // 백업 데이터 유효 기간 설정 (데이터가 없을 때를 대비해 2~3일 정도 유지)

            log.info(">> 오늘 판매 데이터를 백업 키({})로 이관 완료. (누적 X, 단순 교체)", KEY_YESTERDAY_BACKUP);
        } else {
            // 오늘 하나도 안 팔렸다면? -> 기존 백업(어제 데이터)을 지우지 않고 내일도 재사용 (빈 화면 방지)
            log.info(">> 오늘 판매 데이터 없음. 기존 백업 데이터({})를 유지합니다.", KEY_YESTERDAY_BACKUP);
        }

        log.info("=============================================================");
    }
}