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
//    @Scheduled(cron = "0 * * * * *")
    @Scheduled(cron = "0 0 * * * *")
    public void updateBestsellerRanking() {
        log.info("========== [Scheduler] 🥇 베스트셀러 랭킹 집계 시작 ==========");

        ZSetOperations<String, String> zSetOps = bestsellerRedisTemplate.opsForZSet();
        ListOperations<String, String> listOps = bestsellerRedisTemplate.opsForList();

        Set<String> finalRankingSet = new LinkedHashSet<>();
        List<String> logMessages = new ArrayList<>();

        Set<TypedTuple<String>> todayTuples = zSetOps.reverseRangeWithScores(KEY_DAILY_SALES_STATS, 0, 9);
        if (todayTuples != null) {
            for (TypedTuple<String> tuple : todayTuples) {
                String bookId = tuple.getValue();
                Double score = tuple.getScore();
                int salesCount = (score != null) ? score.intValue() : 0;

                finalRankingSet.add(bookId);
                logMessages.add(String.format("📖 도서ID: %s (%d권) *", bookId, salesCount));
            }
        }
        log.info(">> 오늘 판매 데이터: {}건 반영됨", finalRankingSet.size());


        if (finalRankingSet.size() < 10) {
            log.info(">> 데이터 부족 (현재 {}개). 백업 데이터 보충 시도...", finalRankingSet.size());

            Set<TypedTuple<String>> backupTuples = zSetOps.reverseRangeWithScores(KEY_YESTERDAY_BACKUP, 0, 19);

            if (backupTuples != null) {
                for (TypedTuple<String> tuple : backupTuples) {
                    if (finalRankingSet.size() >= 10) break;

                    String bookId = tuple.getValue();
                    if (finalRankingSet.add(bookId)) {
                        Double score = tuple.getScore();
                        int salesCount = (score != null) ? score.intValue() : 0;

                        logMessages.add(String.format("📖 도서ID: %s (%d권)", bookId, salesCount));
                    }
                }
            }
        }

        List<String> finalRankingList = new ArrayList<>(finalRankingSet);
        if (!finalRankingList.isEmpty()) {
            String tempKey = "temp:bestseller:update:" + UUID.randomUUID();

            listOps.rightPushAll(tempKey, finalRankingList);
            bestsellerRedisTemplate.expire(tempKey, 60, java.util.concurrent.TimeUnit.SECONDS);
            bestsellerRedisTemplate.rename(tempKey, KEY_BESTSELLER_VIEW_CACHE);
            bestsellerRedisTemplate.persist(KEY_BESTSELLER_VIEW_CACHE);

            log.info(">> 최종 랭킹 반영 완료 (총 {}권)", finalRankingList.size());

            for (int i = 0; i < logMessages.size(); i++) {
                log.info("{}위 - {}", i + 1, logMessages.get(i));
            }

        } else {
            log.warn(">> 판매 데이터 0건. 베스트셀러 리스트 갱신 없음 (기존 데이터 유지 또는 삭제)");
        }

        log.info("========================================================");
    }

    /**
     * 매일 자정 실행
     */
//    @Scheduled(cron = "0 0/10 * * * *")
    @Scheduled(cron = "0 0 0 * * *")
    public void dailyReset() {
        log.info("========== [Scheduler] 데이터 누적 및 초기화 수행 ==========");

        if (Boolean.TRUE.equals(bestsellerRedisTemplate.hasKey(KEY_DAILY_SALES_STATS))) {
            ZSetOperations<String, String> zSetOps = bestsellerRedisTemplate.opsForZSet();

            zSetOps.unionAndStore(KEY_YESTERDAY_BACKUP, KEY_DAILY_SALES_STATS, KEY_YESTERDAY_BACKUP);

            bestsellerRedisTemplate.delete(KEY_DAILY_SALES_STATS);

            bestsellerRedisTemplate.expire(KEY_YESTERDAY_BACKUP, 7, java.util.concurrent.TimeUnit.DAYS);

            log.info(">> 오늘 판매 데이터를 백업 키({})에 '누적(Union)' 완료.", KEY_YESTERDAY_BACKUP);
        } else {
            log.info(">> 오늘 판매 데이터 없음. 기존 백업 데이터 유지.");
        }

        log.info("=============================================================");
    }
}