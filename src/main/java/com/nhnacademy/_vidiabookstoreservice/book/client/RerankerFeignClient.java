package com.nhnacademy._vidiabookstoreservice.book.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "reranker-client", url = "http://reranker.java21.net")
public interface RerankerFeignClient {

    @PostMapping("/rerank")
    List<RerankResult> rerank(@RequestBody RerankRequest request);


    record RerankRequest(String query, List<String> texts){}
    record RerankResult(int index, double score) {}

}
