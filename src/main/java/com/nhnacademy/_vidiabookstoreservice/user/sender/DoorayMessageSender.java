package com.nhnacademy._vidiabookstoreservice.user.sender;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class DoorayMessageSender {
    private static final String HOOK_URL =
            "https://nhnacademy.dooray.com/services/3204376758577275363/4188019264024021000/-8rBawZ_RuKrQqeTs3Qnpw";

    private final Gson gson = new Gson();

    public void send(String title, String message) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost httpPost = new HttpPost(HOOK_URL);
            httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");

            HookBody hookBody = new HookBody(title, message);
            String json = gson.toJson(hookBody);

            httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                System.out.println("Dooray Response Code: " + response.getCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
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
