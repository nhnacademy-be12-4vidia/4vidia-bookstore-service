package com.nhnacademy._vidiabookstoreservice.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.TossPaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.exception.PaymentConfirmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class TossPaymentService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    ObjectMapper objectMapper = new ObjectMapper();

    @Value("${toss.secretKey}")
    private String API_SECRET_KEY;

    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";


    public TossPaymentResponse confirmPayment(String paymentKey, String orderId, long amount) throws IOException {
        Map<String, Object> requestData = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        Map<String, Object> response = sendRequest(requestData, API_SECRET_KEY, TOSS_CONFIRM_URL);

        if (response.containsKey("code")) {
            throw new PaymentConfirmException(response.toString());
        }

        TossPaymentResponse tossPayment = objectMapper.convertValue(response, TossPaymentResponse.class);

       return tossPayment;
    }

    private Map<String, Object> sendRequest(Map<String, Object> requestData, String secretKey, String urlString) throws IOException {
        HttpURLConnection connection = createConnection(secretKey, urlString);

        try (OutputStream os = connection.getOutputStream()) {
            objectMapper.writeValue(os, requestData);
        }

        try (InputStream responseStream = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
             Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {

            return objectMapper.readValue(reader, new com.fasterxml.jackson.core.type.TypeReference<>() {});

        } catch (Exception e) {
            logger.error("Error reading response or parsing JSON", e);
            return Map.of("code", "COMMUNICATION_ERROR", "message", e.getMessage());
        }
    }

    private HttpURLConnection createConnection(String secretKey, String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        return connection;
    }


}
