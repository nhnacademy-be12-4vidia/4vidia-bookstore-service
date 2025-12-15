package com.nhnacademy._vidiabookstoreservice.book.redis.dto;

public record AiCacheEntry(
        String entryId,
        String keyword,
        String answerJson,
        String vecBase64,
        long createdAtMs
) {
}
