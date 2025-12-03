package com.nhnacademy._vidiabookstoreservice.book.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ollama-client", url = "http://ollama.java21.net")
public interface OllamaFeignClient {

    @PostMapping("/api/embeddings")
    EmbeddingResponse generateEmbedding(@RequestBody EmbeddingRequest request);


    record EmbeddingRequest(String model, String prompt) {}
    record EmbeddingResponse(double[] embedding) {}

}
