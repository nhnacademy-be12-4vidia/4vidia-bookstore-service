package com.nhnacademy._vidiabookstoreservice.book.service.search.embedding;

import com.nhnacademy._vidiabookstoreservice.book.client.OllamaFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaEmbeddingService implements EmbeddingService{

    private final OllamaFeignClient ollamaClient;

    @Override
    public float[] embedOrNull(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }

        try {
            var embeddingRequest = new OllamaFeignClient.EmbeddingRequest("bge-m3", text);
            var embeddingResult = ollamaClient.generateEmbedding(embeddingRequest);
            return toFloatArray(embeddingResult.embedding());
        } catch (Exception e) {
            log.error("Ollama 검색 keyword 임베딩 실패 (키워드 검색만 수행합니다): {}" , e.getMessage());
            return null;
        }
    }

    private float[] toFloatArray(double[] doubles) {
        if (doubles == null) return new float[0];
        float[] floats = new float[doubles.length];
        for (int i = 0; i < doubles.length; i++) {
            floats[i] = (float) doubles[i];
        }
        return floats;
    }
}
