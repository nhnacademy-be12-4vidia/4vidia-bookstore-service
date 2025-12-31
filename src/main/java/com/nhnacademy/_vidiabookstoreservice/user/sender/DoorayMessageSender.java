package com.nhnacademy._vidiabookstoreservice.user.sender;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class DoorayMessageSender {

    // json 문자열 변환용 , dooray webhook은 json형식만 받기 때문에 필수
    private final Gson gson = new Gson();

    /**
     * @param hookUrl Dooray Webhook URL (사용자가 입력한 값)
     * @param title   메시지 제목
     * @param message 메시지 내용
     */
    public void send(String hookUrl, String title, String message) {
        if (hookUrl == null || hookUrl.isBlank()) {
            throw new IllegalArgumentException("Dooray Webhook URL 이 비어있습니다.");
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost httpPost = new HttpPost(hookUrl);   // 고정 URL 제거, 파라미터 사용
            httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");

            HookBody hookBody = new HookBody(title, message);
            String json = gson.toJson(hookBody);

            httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                log.info("Dooray Response Code: {}", response.getCode());
            }

        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class HookBody {
        private String title;
        private String text;
    }
}
