package com.nhnacademy._vidiabookstoreservice.book.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "reranker-client", url = "http://reranker.java21.net")
public interface RerankerFeignClient {

    @PostMapping("/rerank")
    List<RerankResult> rerank(@RequestBody RerankRequest request);


    record RerankRequest(String query, List<String> text){}
    record RerankResult(int index, double score) {}

}
